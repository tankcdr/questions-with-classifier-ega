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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ibm.watson.app.qaclassifier.rest.ConfigurationConstants;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;
import com.ibm.watson.app.qaclassifier.services.rest.impl.ConfigurationBasedConfidenceCategorizer;
import com.ibm.watson.app.common.services.general.ConfigurationService;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationBasedConfidenceCategorizerTest implements ConfigurationConstants {
    @Mock 
    private ConfigurationService mockConfigService; 
    private ConfidenceCategorizer categorizer;    
    private Double confidence;
    private ConfidenceCategoryEnum category;
    
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
    public void test_categorizer_return_for_negative_threshold_confidence_lower_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(-0.50d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
        THEN.an_exception_has_been_raised();
    }
    
    @Test
    public void test_categorizer_return_for_zero_threshold_confidence_lower_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(-1.0d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_low();
    }
    
    @Test
    public void test_categorizer_return_for_zero_threshold_confidence_equal_to_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(0d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_zero_threshold_confidence_greater_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(0.50d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_positive_threshold_confidence_lower_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0.50d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(0.0d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_low();
    }
    
    @Test
    public void test_categorizer_return_for_positive_threshold_confidence_equal_to_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0.50d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(0.50d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_positive_threshold_confidence_greater_than_threshold() {
        GIVEN.the_confidence_threshold_is_set_to(0.50d);
            AND.the_categorizer_is_created_with_configuration_service(mockConfigService);
            AND.the_confidence_value_is(0.51d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    private void an_exception_has_been_raised() {
        // Doc method
    }
    
    private void the_category_is_low() {
        assertEquals(ConfidenceCategoryEnum.LOW, category);
    }

    private void the_category_is_high() {
        assertEquals(ConfidenceCategoryEnum.HIGH, category);
    }

    private void the_category_is_not_null() {
        assertNotNull(category);
    }

    private void categorize_is_invoked() {
        List<Answer> answerList = new ArrayList<>();
        answerList.add(new Answer(null, "", confidence, null, null, null));
        category = categorizer.categorize(answerList).get(0).getConfidenceCategory();
    }

    private void the_confidence_value_is(Double confidence) {
        this.confidence = confidence;
    }
    
    private void the_confidence_threshold_is_set_to(Double threshold) {
        when(mockConfigService.getProperty(HIGH_LOW_CONFIDENCE_THRESHOLD)).thenReturn(String.valueOf(threshold));
    }

    private void the_categorizer_is_created_with_configuration_service(ConfigurationService service) {
        this.categorizer = new ConfigurationBasedConfidenceCategorizer(service);
    }

    @SuppressWarnings("unused")
    private final ConfigurationBasedConfidenceCategorizerTest GIVEN = this, WHEN = this, THEN = this, WITH = this, AND = this;
}
