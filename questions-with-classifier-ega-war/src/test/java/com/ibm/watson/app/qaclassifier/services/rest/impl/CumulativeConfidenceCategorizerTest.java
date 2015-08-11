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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.qaclassifier.rest.ConfigurationConstants;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;

@RunWith(MockitoJUnitRunner.class)
public class CumulativeConfidenceCategorizerTest implements ConfigurationConstants {
    @Mock 
    private ConfigurationService mockConfigService; 
    private ConfidenceCategorizer categorizer;
    
    private List<Double> confidences;
    private List<Answer> answers;
    
    @Test(expected=NullPointerException.class)
    public void test_categorizer_with_null_config_service() {
        GIVEN.the_categorizer_is_created_with_configuration_service(null);
        THEN.an_exception_has_been_raised();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_categorizer_with_no_config_entry() {
        GIVEN.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_categorizer_with_null_config_entry() {
        GIVEN.the_confidence_threshold_is_set_to(null);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_categorizer_with_negative_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(-0.50d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_categorizer_with_positive_threshold_zero_count() {
        GIVEN.the_confidence_threshold_is_set_to(.50d);  
            AND.the_confidence_count_is_set_to(0);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_categorizer_with_positive_threshold_greater_than_max_answers_count() {
        GIVEN.the_confidence_threshold_is_set_to(.50d);  
            AND.the_confidence_count_is_set_to(CumulativeConfidenceCategorizer.MAX_ANSWERS + 1);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test
    public void test_categorizer_with_positive_threshold_exactly_max_answers_count() {
        GIVEN.the_confidence_threshold_is_set_to(.50d);  
            AND.the_confidence_count_is_set_to(CumulativeConfidenceCategorizer.MAX_ANSWERS);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.no_exception_has_been_raised();
    }
    
    @Test
    public void test_categorizer_return_for_first_answer_greater_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0.9d);
            AND.the_confidence_count_is_set_to(3);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_values_are(.91, .04, .02, .02, .01);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_first_answer_lower_than_threshold_sum_is_greater() {
        GIVEN.the_confidence_threshold_is_set_to(0.9d);
            AND.the_confidence_count_is_set_to(3);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_values_are(.89, .04, .02, .02, .01);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_low();
    }
    
    @Test
    public void test_categorizer_return_for_first_answer_lower_than_threshold_sum_is_not_greater() {
        GIVEN.the_confidence_threshold_is_set_to(0.99d);
            AND.the_confidence_count_is_set_to(3);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_values_are(.89, .04, .02, .02, .01);
            AND.categorize_is_invoked();
        THEN.the_answer_list_is_empty();
    }
    
    private void no_exception_has_been_raised() {
        // Doc method
    }
    
    private void an_exception_has_been_raised() {
        // Doc method
    }
    
    private void the_answer_list_is_empty() {
        assertTrue(answers.isEmpty());
    }
    
    private void the_category_is_low() {
        assertEquals(ConfidenceCategoryEnum.LOW, answers.get(0).getConfidenceCategory());
    }

    private void the_category_is_high() {
        assertEquals(ConfidenceCategoryEnum.HIGH, answers.get(0).getConfidenceCategory());
    }

    private void the_category_is_not_null() {
        assertNotNull(answers.get(0).getConfidenceCategory());
    }

    private void categorize_is_invoked() {
        List<Answer> answerList = new ArrayList<>();
        for(Double confidence : confidences) {
            answerList.add(new Answer(null, "", confidence, null, null, null));
        }
        answers = categorizer.categorize(answerList);
    }

    private void the_confidence_values_are(Double ... confidence) {
        this.confidences = Arrays.asList(confidence);
    }
    
    private void the_confidence_count_is_set_to(Integer count) {
        when(mockConfigService.getProperty(CUMULATIVE_CONFIDENCE_COUNT)).thenReturn(String.valueOf(count));
    }
    
    private void the_confidence_threshold_is_set_to(Double threshold) {
        when(mockConfigService.getProperty(HIGH_LOW_CONFIDENCE_THRESHOLD)).thenReturn(String.valueOf(threshold));
    }

    private void the_categorizer_is_created_with_configuration_service(ConfigurationService service) {
        this.categorizer = new CumulativeConfidenceCategorizer(service);
    }

    @SuppressWarnings("unused")
    private final CumulativeConfidenceCategorizerTest GIVEN = this, WHEN = this, THEN = this, WITH = this, AND = this;
}
