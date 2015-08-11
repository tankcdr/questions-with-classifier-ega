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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer.TypeEnum;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassifierTrainingData;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

/**
 * This is a tool/utility class that reads in both an answer.csv file and a questions.csv file and generates
 * the classifier training json file and populates the answer store for the application.  The 2 are generated
 * together to make sure that they are in sync and that every class in the training file has an associated answer 
 * in the answer store.
 * 
 * The CSV formats expected are:
 * questions.csv: QuestionText, LabelId
 * answers.csv: LabelId, AnswerValue, CanonicalQuestion
 * 
 * The LabelId values should match between the 2 files, any LabelIds in the questions.csv that do not appear in the
 * answers.csv will NOT be output in the training file.  Any answers in the answers.csv that do not have an AnswerValue (blank)
 * will not be populated into the database and any questions with that LabelId will also be excluded.
 * 
 * This is meant to be run on the command line at development time, its not a run time utility
 * 
 * @author Stephan J Roorda
 *
 */
public class GenerateTrainingAndPopulationData {

	// JSON OBJECTS
    /*
	static class TrainingData {
		String language;
		List<TrainingInstance> training_data;
		
		public void setLanguage(String language) {
			this.language = language;
		}
		
		public void setInstances(List<TrainingInstance> instances) {
			this.training_data = instances;
		}
		
		public void add(TrainingInstance instance) {
		    if( training_data == null ) {
		        training_data = new ArrayList<TrainingInstance>();
		    }
		    
		    training_data.add(instance);
		}
	}
	
	static class TrainingInstance {
		String text;
		List<String> classes;
		
		public void setText(String text) {
			this.text = text;
		}
		
		public void setLabels(List<String> labels) {
			this.classes = labels;
		}
		
		public void addLabel(String label) {
		    if( classes == null ) {
		        classes = new ArrayList<String>();
		    }
		    
		    classes.add(label);
		}
	}
	*/
/*
    static class Answer {
        String text;
        String className;
        String canonicalQuestion;
        String type = "TEXT";
        
        public void setText(String text) {
            this.text = text;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        public void setCanonicalQuestion(String canonical) {
            this.canonicalQuestion = canonical;
        }
    }
*/
	// options for command line parameters
	private static final String QUESTION_INPUT = "qin", QUESTION_INPUT_LONG = "questionInput";
	private static final String QUESTION_OUTPUT = "qout", QUESTION_OUTPUT_LONG = "questionOutput";
	private static final String ANSWER_INPUT = "ain", ANSWER_INPUT_LONG = "answerInput";
    private static final String ANSWER_OUTPUT = "aout", ANSWER_OUTPUT_LONG = "answerOutput";
	
	// the input and output files that we need
    static File questionInput = null;
    static File questionOutput = null;
    static File answerInput = null;
    static File answerOutput = null;
    
	public static void main(String[] args) {
	    System.out.println(MessageKey.AQWQAC20007I_starting_generate_training_and_populating.getMessage().getFormattedMessage());
	    
		// handle reading the command line parameters and initializing the files
	    readCommandLineParameters(args);
	    System.out.println(MessageKey.AQWQAC20008I_cmd_line_param_read.getMessage().getFormattedMessage());
	    
	    // process the answers input file and create the in-memory store for it
	    List<ManagedAnswer> answers = readAnswerInput();
	    if( answers == null || answers.size() == 0 ) {
	        System.err.println(MessageKey.AQWQAC24010E_answer_store_unable_to_load.getMessage().getFormattedMessage());
	        System.exit(0);
	    }
	    System.out.println(MessageKey.AQWQAC20004I_answer_input_file_read.getMessage().getFormattedMessage());
	    
	    // process the questions input file and create the in-memory store for it
	    NLClassifierTrainingData training = readQuestionInput(answers);
	    if( training == null || training.getTrainingData() == null || training.getTrainingData().size() == 0 ) {
	        System.err.println(MessageKey.AQWQAC24010E_answer_store_unable_to_load.getMessage().getFormattedMessage());
	        System.exit(0);
	    }
        System.out.println(MessageKey.AQWQAC24005I_question_input_file_read.getMessage().getFormattedMessage());
	    
        try {
            // write the answer store population file
            // create the gson object that is doing all the writing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writeGSON(gson.toJson(answers), answerOutput);
            System.out.println(MessageKey.AQWQAC24006I_answer_output_file_written.getMessage().getFormattedMessage());
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
	    
	    try {
	        // write the classifier training file
            writeGSON(training.toJson(), questionOutput);
            System.out.println(MessageKey.AQWQAC24007I_training_data_file_written.getMessage().getFormattedMessage());
        } 
	    catch (IOException e) {
            e.printStackTrace();
        }
	}

	/**
	 * Reads in the answer input file and creates a POJO for each answer it finds.  If the answer has no value
	 * it is skipped.
	 * 
	 * @return AnswerStore - full POJO of the answer store read from the file
	 */
	private static List<ManagedAnswer> readAnswerInput() {
	    List<ManagedAnswer> store = null;
	    
	    try (
	       FileReader reader = new FileReader(answerInput);
	       CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL);
	    ){
	        // read in the csv file and get the records
	        List<CSVRecord> records = parser.getRecords();
	        
	        // now we can create the answer store because we have read the records
	        store = new ArrayList<ManagedAnswer>();
	        for( CSVRecord r : records ) {
	            // order is: LabelId, AnswerValue, CanonicalQuestion
	            
	            // check for AnswerValue first, if not there, skip
	            String text = r.get(1);
	            if( text == null || text.isEmpty() ) {
	                continue;
	            }
	            
	            // create the answer pojo
	            ManagedAnswer answer = new ManagedAnswer();
	            answer.setClassName(r.get(0));
	            answer.setText(r.get(1));
	            answer.setCanonicalQuestion(r.get(2));
	            answer.setType(TypeEnum.TEXT);
	            
	            // add to the answer store only if there is answer text
	            if( answer.getText().isEmpty() ) {
	            	
                  System.out.println(MessageKey.AQWQAC20007I_answer_text_is_empty_for_entry_2.getMessage(answer.getClassName(), answer.getCanonicalQuestion()).getFormattedMessage());  
	            }
	            else {
                  store.add(answer);
	            }
	        }
	    }
	    catch(Exception e) {
	        e.printStackTrace();
	    }
	    
        return store;
	}
	
	/**
	 * Reads in the question input file and creates a POJO for each question it finds.  If the label associated with 
	 * the question does not exist in the previously read in answer store then it is skipped
	 * 
	 * @return TrainingData - full POJO of the training data
	 */
	private static NLClassifierTrainingData readQuestionInput(List<ManagedAnswer> store) {
	    NLClassifierTrainingData data = null;

        try (
           FileReader reader = new FileReader(questionInput);
           CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)
        ){
            // read in the csv file and get the records            
            List<CSVRecord> records = parser.getRecords();
            
            // now we can create the training data because we have read the records
            data = new NLClassifierTrainingData();
            data.setLanguage("en");
            for( CSVRecord r : records ) {
                // order is: QuestionText, LabelId
                
                // check for existence of label first, if not there, skip                
                // we only add the training instance if there is an associated answer
                String text = r.get(0);
                String label = r.get(1);
                if( labelHasAnswer(label, store) ) {
                    data.addTrainingData(text, label);      
                }
                else {
                    System.out.println(MessageKey.AQWQAC24009E_label_not_found_in_answer_store_including_2.getMessage(text, label).getFormattedMessage());
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
	    
	    return data;
	}
	
    private static boolean labelHasAnswer(String label, List<ManagedAnswer> answers) {
        boolean result = false;
        
        for( ManagedAnswer a : answers ) {
            if( a.getClassName().equals(label) ) {
                result = true;
                break;
            }
        }
        
        return result;
    }

	private static void writeGSON(String src, File output) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(output);
            writer.write(src);
            writer.flush();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }	    
	}
	
    /**
     * This method handles all of the parsing and validation of the required input parameters.  If any of the files do not exist or cannot be created
     * in the specified location then the method will fail.  If we return from this method then we have all the files properly.
     * 
     * @param args
     * @throws IllegalArgumentException
     */
    private static void readCommandLineParameters(String[] args) throws IllegalArgumentException {
        Option questionInputOption = createOption(QUESTION_INPUT, QUESTION_INPUT_LONG, true, "input csv file containing questions and labels", true, QUESTION_INPUT_LONG);
        Option questionOutputOption = createOption(QUESTION_OUTPUT, QUESTION_OUTPUT_LONG, true, "filename and location for the classifier training data", true, QUESTION_OUTPUT_LONG);
        Option answerInputOption = createOption(ANSWER_INPUT, ANSWER_INPUT_LONG, true, "input csv file containing answers data", true, ANSWER_INPUT_LONG);
        Option answerOutputOption = createOption(ANSWER_OUTPUT, ANSWER_OUTPUT_LONG, true, "filename and location for the answer store population data", true, ANSWER_OUTPUT_LONG);
        
        final Options options = buildOptions(questionInputOption, questionOutputOption, answerInputOption, answerOutputOption);

        CommandLine cmd;
        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);
        } 
        catch(ParseException e) {
        	System.err.println(MessageKey.AQWQAC24008E_could_not_parse_cmd_line_args_1.getMessage(e.getMessage()).getFormattedMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(120, "java " + GenerateTrainingAndPopulationData.class.getName(), null, options, null);
            return;
        }

        // before we do anything else make sure we can read and write all of the necessary files
        final String questionInputFile = cmd.getOptionValue(QUESTION_INPUT).trim();
        final String questionOutputFile = cmd.getOptionValue(QUESTION_OUTPUT).trim();
        final String answerInputFile = cmd.getOptionValue(ANSWER_INPUT).trim();
        final String answerOutputFile = cmd.getOptionValue(ANSWER_OUTPUT).trim();
                
        // make sure we have all 4 parameters
        if( questionInputFile.isEmpty() || questionOutputFile.isEmpty() || answerInputFile.isEmpty() || answerOutputFile.isEmpty() ) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14200E_must_specify_4_files.getMessage().getFormattedMessage());
            
        }
        
        // make sure the question input file exists
        questionInput = new File(questionInputFile);
        if( !questionInput.exists() ) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14201E_file_does_not_exist_1.getMessage(questionInput.getAbsolutePath()).getFormattedMessage());
        }
        
        // make sure the answer input file exists
        answerInput = new File(answerInputFile);
        if( !answerInput.exists() ) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14201E_file_does_not_exist_1.getMessage(answerInput.getAbsolutePath()).getFormattedMessage());
        }
        
        // make sure we can create the question output file
        questionOutput = new File(questionOutputFile);
        if( !questionOutput.getParentFile().exists() && !questionOutput.getParentFile().mkdirs() ) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14202E_unable_create_parent_dir_for_file_1.getMessage(questionOutput.getAbsolutePath()).getFormattedMessage());
        }       

        // make sure we can create the question output file
        answerOutput = new File(answerOutputFile);
        if( !answerOutput.getParentFile().exists() && !answerOutput.getParentFile().mkdirs() ) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14202E_unable_create_parent_dir_for_file_1.getMessage(answerOutput.getAbsolutePath()).getFormattedMessage());
        }       
    }
    
    private static Option createOption(String opt, String longOpt, boolean hasArg, String description, boolean required, String argName) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        option.setArgName(argName);
        return option;
    }

    private static Options buildOptions(Option option, Option ... additionalOptions) {
        final Options options = new Options();
        options.addOption(option);
        for(Option o : additionalOptions) {
            options.addOption(o);
        }
        return options;
    }

}
