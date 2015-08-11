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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;

public class DefaultConfidenceCategorizerTest {
    
    private ConfidenceCategorizer categorizer;
    private Double confidence;
    private ConfidenceCategoryEnum category;
    
    @Test
    public void test_categorizer_return_for_null_confidence() {
        GIVEN.the_categorizer_is_created();
            AND.the_confidence_value_is(null);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_negative_confidence() {
        GIVEN.the_categorizer_is_created();
            AND.the_confidence_value_is(-0.50d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_zero_percent_confidence() {
        GIVEN.the_categorizer_is_created();
            AND.the_confidence_value_is(0.0d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }

    @Test
    public void test_categorizer_return_for_50_percent_confidence() {
        GIVEN.the_categorizer_is_created();
            AND.the_confidence_value_is(0.50d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
    }
    
    @Test
    public void test_categorizer_return_for_100_percent_confidence() {
        GIVEN.the_categorizer_is_created();
            AND.the_confidence_value_is(1.0d);
            AND.categorize_is_invoked();
        THEN.the_category_is_not_null();
            AND.the_category_is_high();
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

    private void the_categorizer_is_created() {
        this.categorizer = new DefaultConfidenceCategorizer();
    }

    @SuppressWarnings("unused")
    private final DefaultConfidenceCategorizerTest GIVEN = this, WHEN = this, THEN = this, WITH = this, AND = this;
}
