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

package com.ibm.watson.app.qaclassifier.services.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Question;
import com.ibm.watson.app.qaclassifier.services.answer.AnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.ResolutionException;
import com.ibm.watson.app.qaclassifier.services.rest.QuestionNotFoundException;
import com.ibm.watson.app.qaclassifier.services.rest.TopQuestionsStore;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

/**
 * Loads a list of top questions from a json file on the classpath.
 */
public class TopQuestionsJsonStore implements TopQuestionsStore {

    private static final Logger logger = LogManager.getLogger();
    private static final String TOP_QUESTIONS_FILE = "/top-questions.json";

    static final Map<String, Question> topQuestions = loadTopQuestions();    
    private final AnswerResolver resolver;
    
    @Inject
    public TopQuestionsJsonStore(AnswerResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<Question> getTopQuestions() {
        return ImmutableList.copyOf(topQuestions.values()); 
    }
    
    @Override
    public Answer getAnswer(String questionText) throws QuestionNotFoundException, ResolutionException {
        if(!topQuestions.containsKey(questionText)) {
            throw new QuestionNotFoundException(questionText);
        }
        
        final Question question = topQuestions.get(questionText);        
        NLClassifiedClass classifiedClass = new NLClassifiedClass();
        classifiedClass.setClassName(question.getClassName());
        classifiedClass.setConfidence(100.00);
        return resolver.resolve(classifiedClass);
    }

    private static Map<String, Question> loadTopQuestions() {
        try (InputStream jsonStream = TopQuestionsJsonStore.class.getResourceAsStream(TOP_QUESTIONS_FILE)) {
            if (jsonStream == null) {
            	logger.error(MessageKey.AQWQAC24001E_unable_load_file_from_classpath_1.getMessage(TOP_QUESTIONS_FILE));
                return Collections.emptyMap();
            }

            List<Question> topQuestionList = readStream(jsonStream);
            if (topQuestionList.isEmpty()) {
            	logger.warn(MessageKey.AQWQAC22000W_no_top_questions_found_in_file_1.getMessage(TOP_QUESTIONS_FILE));
            }
            
            // Create this map to easily look up a Question based on the exact question text
            // Using a LinkedHashMap to preserve the order that is present in the JSON file
            Map<String, Question> topQuestionsMap = new LinkedHashMap<>();
            for(Question question : topQuestionList) {
                topQuestionsMap.put(question.getQuestionText(), question);
            }
            return topQuestionsMap;            
        } catch (IOException e) {
            logger.catching(e);
        }
        return Collections.emptyMap();
    }


    private static List<Question> readStream(InputStream jsonStream) throws IOException {
        @SuppressWarnings("serial")
        Type listOfQuestionsType = new TypeToken<List<Question>>() {}.getType();
        try (InputStreamReader streamReader = new InputStreamReader(jsonStream, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(streamReader, listOfQuestionsType);
        } catch (JsonSyntaxException e) {
        	logger.error(MessageKey.AQWQAC24011E_exception_parsing_file_1.getMessage(TOP_QUESTIONS_FILE), e);
            return Collections.emptyList();
        }
    }
}
