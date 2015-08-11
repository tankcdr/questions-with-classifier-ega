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

package com.ibm.watson.app.qaclassifier.rest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class CanonicalQuestion {

	// All of the training data, including text and class mappings
	public class Training {
		private String language;
		private QuestionMapping[] training_data;
		
		// Getters and setters
		public void setLanguage(String language) {
			this.language = language;
		}
		
		public String getLanguage() {
			return this.language;
		}
		
		public void setTrainingData(QuestionMapping[] trainingData) {
			this.training_data = trainingData;
		}
		
		public QuestionMapping[] getTrainingData() {
			return this.training_data;
		}
	}
	
	// An individual text and class mapping
	public class QuestionMapping {
		private String text;
		private String[] classes;
		
		public void setText(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public void setClasses(String[] classes) {
			this.classes = classes;
		}
		
		public String[] getClasses() {
			return this.classes;
		}
	}
	
    private static final Logger logger = LogManager.getLogger();
    
	private static Map<String, String> questionMapping;
	
	private CanonicalQuestion() {}
	
	public static void generateQuestionAnswerPairs() {
		questionMapping = new HashMap<String, String>();
		getJsonFromFile("/training.json");
	}
	
	public static void generateQuestionAnswerPairs(String json) {
		questionMapping = new HashMap<String, String>();
		buildMapping(json);
	}
	
	public static Map<String, String> getQuestionMapping() {
		return questionMapping;
	}
	
	private static void getJsonFromFile(String path) {
		String jsonData;
		
		try {
			jsonData = readFile(path, StandardCharsets.UTF_8);
			buildMapping(jsonData);
		}
		catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private static void buildMapping(String json) {
		// Map the each question to the class listed.  If the class already has a question
		// in our mapping, skip it.
		Gson     gson         = new GsonBuilder().setPrettyPrinting().create();
		Training trainingData = gson.fromJson(json, Training.class);
		
		for (QuestionMapping question : trainingData.training_data) {
			String topClass = question.classes[0];
			
			if (questionMapping.get(topClass) == null) {
				questionMapping.put(topClass, question.text);
			}
		}
	}
	
	private static String readFile(String path, Charset encoding) 
		throws IOException, FileNotFoundException {

		InputStream stream = CanonicalQuestion.class.getResourceAsStream(path);
		BufferedReader r = new BufferedReader(new InputStreamReader(stream, encoding));
		
        String jsonFile       = null;
        StringBuilder builder = new StringBuilder(8192);
        
        while ((jsonFile = r.readLine()) != null) {
        	builder.append(jsonFile);
        }
        
        return builder.toString();
	}

}

