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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Question;
import com.ibm.watson.app.qaclassifier.services.answer.AnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.ResolutionException;
import com.ibm.watson.app.qaclassifier.services.rest.QuestionNotFoundException;
import com.ibm.watson.app.qaclassifier.services.rest.TopQuestionsStore;

@RunWith(MockitoJUnitRunner.class)
public class TopQuestionsJsonStoreTest {
    
    private static final String QUESTION_TEXT_TO_FAIL_RESOLUTION = "This question is a placeholder to fail answer resolution.";
    private static final String CLASS_TO_FAIL_RESOLUTION = "fail_resolution";
    
    private static final String VALID_QUESTION = "This is a placeholder for a valid top question";
    private static final String VALID_ANSWER = "This is the answer for the valid question placeholder";
    
    @Mock
    private AnswerResolver resolver;
    
    @BeforeClass
    public static void setup() {
        // Add into the store some testing questions
        TopQuestionsJsonStore.topQuestions.put(QUESTION_TEXT_TO_FAIL_RESOLUTION, new Question(QUESTION_TEXT_TO_FAIL_RESOLUTION, CLASS_TO_FAIL_RESOLUTION));
        TopQuestionsJsonStore.topQuestions.put(VALID_QUESTION, new Question(VALID_QUESTION, VALID_ANSWER));
    }
    
    @AfterClass
    public static void cleanup() {
        TopQuestionsJsonStore.topQuestions.remove(QUESTION_TEXT_TO_FAIL_RESOLUTION);
        TopQuestionsJsonStore.topQuestions.remove(VALID_QUESTION);
    }
    
    @Test
    public void getTopQuestions_returns_top_questions() throws Exception {
        TopQuestionsStore store = getStore();        
        List<Question> topQuestions = store.getTopQuestions();
        assertThat(topQuestions, notNullValue());
        assertThat(topQuestions, not(empty()));
    }
    
    @Test(expected=QuestionNotFoundException.class)
    public void get_answer_for_invalid_question_throws_exception() throws Exception {
        TopQuestionsStore store = getStore();
        store.getAnswer("This question is not going to be in the top questions JSON file so it should throw an exception.");
    }
    
    @Test(expected=ResolutionException.class)
    public void get_answer_for_valid_question_no_resolution_throws_exception() throws Exception {
        TopQuestionsStore store = getStore();
        store.getAnswer(QUESTION_TEXT_TO_FAIL_RESOLUTION);
    }
    
    @Test
    public void get_answer_for_valid_question() throws Exception {
        TopQuestionsStore store = getStore();
        Answer answer = store.getAnswer(VALID_QUESTION);
        ArgumentCaptor<NLClassifiedClass> cap = ArgumentCaptor.forClass(NLClassifiedClass.class);
        verify(resolver, times(1)).resolve(cap.capture());
        assertThat(answer, notNullValue());
        assertEquals(VALID_ANSWER, cap.getValue().getClassName());
    }

    private TopQuestionsStore getStore() throws ResolutionException {
        when(resolver.resolve(any(NLClassifiedClass.class))).thenAnswer(new org.mockito.stubbing.Answer<Answer>() {
            @Override
            public Answer answer(InvocationOnMock invocation) throws Throwable {
                String className = invocation.getArgumentAt(0, NLClassifiedClass.class).getClassName();
                if(className.equals(CLASS_TO_FAIL_RESOLUTION)) {
                    throw new ResolutionException(CLASS_TO_FAIL_RESOLUTION);
                }
                
                // Mock a successful resolution
                return mock(Answer.class);
            }
        });
        return new TopQuestionsJsonStore(resolver);
    }
}
