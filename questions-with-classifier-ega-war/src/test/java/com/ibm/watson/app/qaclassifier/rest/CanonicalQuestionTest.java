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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.ibm.watson.app.qaclassifier.rest.CanonicalQuestion;

public class CanonicalQuestionTest {

	@Test
	public void testGenerateQuestionAnswerPairs() {
		CanonicalQuestion.generateQuestionAnswerPairs();
		Map<String, String> mapping = CanonicalQuestion.getQuestionMapping();

		verifyMappingCreation(mapping);
	}

	@Test
	public void testGenerateQuestionAnswerPairsString() {
		String mockJson = "{ 'language': 'en','training_data': [{'text': 'question','classes': ['{class1}']}]}";
		
		CanonicalQuestion.generateQuestionAnswerPairs(mockJson);
		Map<String, String> mapping = CanonicalQuestion.getQuestionMapping();
		
		verifyMappingCreation(mapping);
		
		assertEquals(mapping.get("{class1}"), "question");
	}
	
	private void verifyMappingCreation(Map<String, String> mapping) {
		assertNotNull(mapping);
		assertFalse(mapping.isEmpty());
		assertTrue(mapping.size() > 0);
	}
}
