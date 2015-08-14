/*
 * Copyright IBM Corp. 2015
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.watson.app.qaclassifier.services.rest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

/**
 * This class implements the cumulative confidence algorithm suggested by the classifier team.
 * An implementation that inspects more than just a single answer is recommended since the confidence 
 * values are not independent from one another, they all sum to 100%. This means that it is impossible to
 * obtain two answers with greater than 50% confidence. 
 * 
 * The algorithm is as follows:
 * 
 * It first checks the top answer to see if it is above some threshold. 
 * If so, the response is considered high confidence.
 * 
 * Then, it sums the confidence values of the top N answers, to see if the sum is greater than some threshold.
 * If so, the response is considered low confidence.
 * The theory is that the first answer is not guaranteed to be correct, but the correct answer should be in the top N.
 * 
 * Otherwise, we clear the answer list and return no answers, as we are not confident enough to have a valid response.
 */
public class CumulativeConfidenceCategorizer extends ConfigurationBasedConfidenceCategorizer {
    private static final Logger logger = LogManager.getLogger();
    
    static final int MAX_ANSWERS = 10;     
    protected final int count;
    
    @Inject
    public CumulativeConfidenceCategorizer(ConfigurationService config) {
        super(config);
        
        String countValue = config.getProperty(CUMULATIVE_CONFIDENCE_COUNT);
        if(countValue == null) {
            throw new IllegalArgumentException(MessageKey.AQWQAC24151E_property_must_be_set_in_conf_1.getMessage(CUMULATIVE_CONFIDENCE_COUNT).getFormattedMessage());
        }
        
        this.count = Integer.parseInt(countValue, 10);
        if(this.count <= 0 || this.count > MAX_ANSWERS) {
            throw new IllegalArgumentException(MessageKey.AQWQAC24152E_invalid_count_value_specified_must_in_range_1.getMessage(MAX_ANSWERS).getFormattedMessage());
        }
        logger.debug("Confidence count = " + count);
    }
    
    @Override
    public List<Answer> categorize(List<Answer> answersIn) {
        
        if(answersIn.size() == 0) {
            return Collections.emptyList();
        }
        
        int answerCount = Math.min(answersIn.size(), count);
        
        // If first answer is greater than the threshold, HIGH
        Answer firstAnswer = answersIn.get(0);
        if(firstAnswer.getConfidence() >= threshold) {
        	List<Answer> answers = new ArrayList<>();
        	// Return top answer plus $count refinement suggestions
        	for (int i = 0; i < Math.min(answerCount + 1, answersIn.size()); i++) {
        		Answer answer = answersIn.get(i);
        		answer.setConfidenceCategory(ConfidenceCategoryEnum.HIGH);
        		answers.add(answer);
        	}
            return answers;
        }
        
        // If sum of top N answers confidence > threshold, LOW
        double sum = 0.0d;
        for(int i = 0; i < answerCount; i++) {
            sum += answersIn.get(i).getConfidence();
        }
        if(sum >= threshold) {
        	final List<Answer> answers = new ArrayList<>();
        	for (int i = 0; i < answerCount; i++) {
        		Answer answer = answersIn.get(i);
        		answer.setConfidenceCategory(ConfidenceCategoryEnum.LOW);
        		answers.add(answer);
        	}
            return answers;
        }
        
        // Otherwise, return an empty list
        return Collections.emptyList();
    }
}
