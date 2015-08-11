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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.qaclassifier.rest.ConfigurationConstants;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class ConfigurationBasedConfidenceCategorizer implements ConfidenceCategorizer, ConfigurationConstants {
    private static final Logger logger = LogManager.getLogger();
    
    protected final Double threshold;
    
    @Inject
    public ConfigurationBasedConfidenceCategorizer(ConfigurationService config) {
        Objects.requireNonNull(config, MessageKey.AQWQAC24012E_conf_service_null.getMessage().getFormattedMessage());
        
        String thresholdValue = config.getProperty(HIGH_LOW_CONFIDENCE_THRESHOLD);
        if(thresholdValue == null) {
            throw new IllegalArgumentException(MessageKey.AQWQAC24002E_property_must_be_set_in_conf_1.getMessage(HIGH_LOW_CONFIDENCE_THRESHOLD).getFormattedMessage());
        }
        
        this.threshold = Double.parseDouble(thresholdValue);
        if(this.threshold < 0.0d || this.threshold > 1.0d) {
        	
            throw new IllegalArgumentException(MessageKey.AQWQAC24003E_invalid_threshold_value_specified.getMessage().getFormattedMessage());
        }
        logger.debug("Confidence threshold = " + threshold);
    }

    @Override
    public List<Answer> categorize(List<Answer> answersIn) {
        final List<Answer> answers = new ArrayList<>(answersIn);
        for(Answer answer : answers) {
            final Double confidence = answer.getConfidence();
            ConfidenceCategoryEnum confidenceCategory = (confidence >= threshold) ? ConfidenceCategoryEnum.HIGH : ConfidenceCategoryEnum.LOW;
            answer.setConfidenceCategory(confidenceCategory);
        }
        return answers;
    }
}
