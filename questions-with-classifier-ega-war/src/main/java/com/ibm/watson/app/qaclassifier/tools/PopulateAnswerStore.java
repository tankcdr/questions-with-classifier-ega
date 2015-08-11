/* Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.watson.app.qaclassifier.tools;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.watson.app.common.util.rest.BooleanResponseHandler;
import com.ibm.watson.app.common.util.rest.JSONEntity;
import com.ibm.watson.app.common.util.rest.SimpleRestClient;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer.TypeEnum;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class PopulateAnswerStore {
	public static final String DEFAULT_URL = "http://localhost:9080";
	private static final String DEFAULT_ENDPOINT = "/api/v1/manage/answer";

	private static final String URL_OPTION = "l", URL_OPTION_LONG = "url";
	private static final String FILE_OPTION = "f", FILE_OPTION_LONG = "file";
	private static final String DIR_OPTION = "d", DIR_OPTION_LONG = "directory";
	private static final String USER_OPTION = "u", USER_OPTION_LONG = "user";
	private static final String PASSWORD_OPTION = "p", PASSWORD_OPTION_LONG = "password";
	
	private static final Gson gson = new Gson();

	public static void main(String[] args) throws Exception {
		Option urlOption = createOption(URL_OPTION, URL_OPTION_LONG, true, "The root URL of the application to connect to. If omitted, the default will be used (" + DEFAULT_URL + ")", false, URL_OPTION_LONG);
		Option fileOption = createOption(FILE_OPTION, FILE_OPTION_LONG, true, "The file to be used to populate the answers, can point to the file system or the class path", true, FILE_OPTION_LONG);
        Option dirOption = createOption(DIR_OPTION, DIR_OPTION_LONG, true, "The directory containing the html answer files, can point to the file system or the class path", true, DIR_OPTION_LONG);
		Option userOption = createOption(USER_OPTION, USER_OPTION_LONG, true, "The username for the manage API", true, USER_OPTION_LONG);
		Option passwordOption = createOption(PASSWORD_OPTION, PASSWORD_OPTION_LONG, true, "The password for the manage API", true, PASSWORD_OPTION_LONG);
		
		final Options options = buildOptions(urlOption, fileOption, dirOption, userOption, passwordOption);

		CommandLine cmd;
		try {
			CommandLineParser parser = new GnuParser();
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			System.err.println(MessageKey.AQWQAC24008E_could_not_parse_cmd_line_args_1.getMessage(e.getMessage()).getFormattedMessage());
			
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(120, "java " + PopulateAnswerStore.class.getName(), null, options, null);
			return;
		}
		
		final String url = cmd.getOptionValue(URL_OPTION, DEFAULT_URL);
		final String file = cmd.getOptionValue(FILE_OPTION);
		final String dir = cmd.getOptionValue(DIR_OPTION);
		final String user = cmd.getOptionValue(USER_OPTION);
		final String password = cmd.getOptionValue(PASSWORD_OPTION);
		
		System.out.println(MessageKey.AQWQAC20002I_checking_answer_store_at_url_2.getMessage(url, DEFAULT_ENDPOINT).getFormattedMessage());
		
		try {
		    AnswerStoreRestClient client = new AnswerStoreRestClient(url, user, password);
		    
		    // we only want to populate if there is nothing in the database already
		    // start with the assumption that we do want to populate and stop if we find answers in there already
		    boolean doPopulate = true;
		    String answersResult = getAnswers(client);
		    if( answersResult != null && !answersResult.isEmpty() ) {
		        Gson gson = new Gson();
		        Type type = new TypeToken<List<ManagedAnswer>>() {}.getType();
		        List<ManagedAnswer> answers = gson.fromJson(answersResult, type);
		        if( answers != null && answers.size() > 0 ) {	        	
		            System.out.println(MessageKey.AQWQAC20006I_found_answers_in_stop_1.getMessage(answers.size()).getFormattedMessage());
		            doPopulate = false;
		        }
		    }
		    
		    if( doPopulate ) {
	            System.out.println(MessageKey.AQWQAC20003I_populating_answer_store_at_url_2.getMessage(url,DEFAULT_ENDPOINT).getFormattedMessage());
		        boolean success = populate(client, file, dir);
		        if(!success) {		        	
		            throw new RuntimeException(MessageKey.AQWQAC24005E_error_populating_answer_store.getMessage().getFormattedMessage());
		        }
		    }
		    else {
                System.out.println(MessageKey.AQWQAC20001I_answer_store_already_populated_doing_nothing.getMessage().getFormattedMessage());
		    }
		    
		    System.out.println(MessageKey.AQWQAC20005I_done_population_answers.getMessage().getFormattedMessage());
		} catch(IOException e) {
		    System.err.println(MessageKey.AQWQAC24007E_error_populating_answer_store_1.getMessage(e.getMessage()).getFormattedMessage());
		    e.printStackTrace(System.err);
		}
	}
	
	private static String getAnswers(AnswerStoreRestClient client) throws IOException {
	    return client.get(DEFAULT_ENDPOINT);
	}
	
	private static boolean populate(AnswerStoreRestClient client, String file, String dir) throws IOException {
		// loads the answers.json file which has class, text, type, canonicalQuestion
	    List<ManagedAnswer> data = loadData(file);
	    
	    // we now load the text (which is the answer) from an html file
	    for(ManagedAnswer a : data ) {
	    	String htmlFile = dir + "/" + a.getClassName() + ".html";
	    	System.out.println(MessageKey.AQWQAC20000I_reading_and_setting_formated_text_from_1.getMessage(htmlFile).getFormattedMessage());
	    	String formattedText = FileUtils.loadFromFilesystemOrClasspath(htmlFile);
	    	a.setText(formattedText);
	    }

	    // While we are at it, the mocked classifier service adds in "defaultClass", so lets set that too
	    ManagedAnswer defaultAnswer = new ManagedAnswer("defaultClass", TypeEnum.TEXT, "This answer is resolved from the default class", "Sample canonical question");
	    data.add(defaultAnswer);
	    
        ResponseHandler<Boolean> handler = new BooleanResponseHandler();    
	    return client.post(DEFAULT_ENDPOINT, new JSONEntity(gson.toJson(data)), handler);
	}
	
	private static List<ManagedAnswer> loadData(String path) throws IOException {
        String csv = FileUtils.loadFromFilesystemOrClasspath(path);
        List<ManagedAnswer> answers = readAnswerInput(csv);
        if(answers == null) {
            throw new IllegalArgumentException(MessageKey.AQWQAC24006E_invalid_schema_answers_json.getMessage().getFormattedMessage());
        }
        
        return answers;
    }

	/**
	 * Reads in the answer input file and creates a POJO for each answer it finds.  If the answer has no value
	 * it is skipped.
	 * 
	 * @return AnswerStore - full POJO of the answer store read from the file
	 */
	private static List<ManagedAnswer> readAnswerInput(String content) {
	    List<ManagedAnswer> store = null;
	    
	    // read the CVS of label to canonical question first
	    try (
	       StringReader reader = new StringReader(content);
	       CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL);
	    ){
	        // read in the csv file and get the records
	        List<CSVRecord> records = parser.getRecords();
	        
	        // now we can create the answer store because we have read the records
	        store = new ArrayList<ManagedAnswer>();
	        for( CSVRecord r : records ) {
	            // order is: LabelId, CanonicalQuestion
	            
	            // create the answer pojo
	            ManagedAnswer answer = new ManagedAnswer();
	            answer.setClassName(r.get(0));
	            answer.setCanonicalQuestion(r.get(1));
	            answer.setType(TypeEnum.TEXT);
	            
	            // add to the managed answers list
                store.add(answer);
	        }
	    }
	    catch(Exception e) {
	        e.printStackTrace();
	    }

	    return store;
	}

    private static class AnswerStoreRestClient extends SimpleRestClient {
        public AnswerStoreRestClient(String url, String user, String password) {
            super(url, user, password);
        }
        
        public <T> T post(String endpoint, HttpEntity entity, ResponseHandler<? extends T> responseHandler) throws IOException {
            return super.post(endpoint, entity, responseHandler);
        }
        
        public String get(String endpoint) throws IOException {
            return super.get(endpoint);
        }
	}
	    
	// These are copied from common tools because I refuse to add a dependency to a tools project that isn't going to be used at runtime
	public static Options buildOptions(Option option, Option ... additionalOptions) {
        final Options options = new Options();
        options.addOption(option);
        for(Option o : additionalOptions) {
            options.addOption(o);
        }
        return options;
    }
    
    public static Option createOption(String opt, String longOpt, boolean hasArg, String description, boolean required, String argName) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        option.setArgName(argName);
        return option;
    }
}
