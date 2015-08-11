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

package com.ibm.watson.app.qaclassifier.rest.impl;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifier;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifierService;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.ConfigurationConstants;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer.SourceEnum;
import com.ibm.watson.app.qaclassifier.rest.model.ErrorResponse;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequest;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer;
import com.ibm.watson.app.qaclassifier.rest.model.MessageResponse;
import com.ibm.watson.app.qaclassifier.rest.model.Question;
import com.ibm.watson.app.qaclassifier.rest.model.TopQuestionsResponse;
import com.ibm.watson.app.qaclassifier.services.answer.AnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.ResolutionException;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;
import com.ibm.watson.app.qaclassifier.services.rest.ConversationManager;
import com.ibm.watson.app.qaclassifier.services.rest.QuestionNotFoundException;
import com.ibm.watson.app.qaclassifier.services.rest.ResponseCache;
import com.ibm.watson.app.qaclassifier.services.rest.TopQuestionsStore;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Response.class})
public class ConversationApiImplTest extends BaseRestApiTest {
    
    private static final String SERVICE_NAME = "test-classifier-service";
    private static final String SERVICE_NAME_2 = "test-classifier-service-2";
    
    private static final Question TOP_QUESTION = new Question("Example top question", "Example answer");
    private static final Question REFINEMENT_QUESTION = new Question("Example refinement question", "Example refinement answer");
    
    private static final String CONVERSATION_ID = "1";
    
    private ConversationApiImpl api;
    private Response response;

    private Set<NLClassifierService> services;
    private AnswerResolver resolver;
    private UserTracking tracking;
    private ConfigurationService config;
    private List<NLClassifier> classifiers;
    private NLClassifier trainingClassifier, availableClassifier;
    private TopQuestionsStore topQuestionsStore;
    private ConversationManager conversationManager;
    private ResponseCache responseCache;
    
    @Mock
    private NLClassiferClassifyResponse classifyResponse;
    private List<NLClassifiedClass> classes;
    private ConfidenceCategorizer categorizer;
    
    @Before
    public void set_up_mocks() throws Exception {
        response_builder_class_is_mocked();
        user_tracking_is_mocked();
        the_answer_resolver_is_mocked(ConfidenceCategoryEnum.HIGH);
        config_is_mocked();
        top_questions_store_is_mocked();
        the_conversation_manager_is_mocked();
        categorizer_is_mocked();
        response_cache_is_mocked();
    }
    
    @Test
    public void test_classifier_service_is_empty() throws Exception {
        GIVEN.the_classifier_service_is_empty();
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.a_server_error_is_returned();
            AND.the_response_entity_is_an_error_respone();
            AND.the_error_message_is(MessageKey.AQWQAC14001E_error_selection_correct_classifier.getMessage().getFormattedMessage());
    }
    
    @Test
    public void test_classifier_service_has_no_instances() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.a_server_error_is_returned();
            AND.the_response_entity_is_an_error_respone();
            AND.the_error_message_is(MessageKey.AQWQAC14002E_no_classifier_instances.getMessage().getFormattedMessage());
    }
    
    @Test
    public void test_classifier_service_has_no_available_instances() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.a_training_classifier_instance();
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.a_server_error_is_returned();
            AND.the_response_entity_is_an_error_respone();
            AND.the_error_message_is(MessageKey.AQWQAC14003E_no_available_classifiers.getMessage().getFormattedMessage());
    }
    
    @Test
    public void test_classifier_service_only_one_available_instance_no_answers() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(0);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_classifier_service_only_one_available_instance_one_answer() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer", 12.34d);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(1);
                AND.verify_the_answer_list_contains_answer("example answer", 12.34d, ConfidenceCategoryEnum.HIGH);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_classifier_service_only_one_available_instance_multiple_answers() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer", 12.34d);
                WITH.answer("example answer 2", 34.56d);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(2);
                AND.verify_the_answer_list_contains_answer("example answer", 12.34d, ConfidenceCategoryEnum.HIGH);
                AND.verify_the_answer_list_contains_answer("example answer 2", 34.56d, ConfidenceCategoryEnum.HIGH);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_classifier_service_one_training_instance_one_available_instance_no_answers() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.a_training_classifier_instance();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_classify_was_not_invoked_on(trainingClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(0);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_two_classifier_services_with_no_specified_name() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            AND.a_second_classifier_service_is_mocked();
            AND.configured_classifier_name_is(null);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.a_server_error_is_returned();
            AND.the_response_entity_is_an_error_respone();
            AND.the_error_message_is(MessageKey.AQWQAC14001E_error_selection_correct_classifier.getMessage().getFormattedMessage());
    }
    
    @Test
    public void test_two_classifier_services_with_invalid_name() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            AND.a_second_classifier_service_is_mocked();
            AND.configured_classifier_name_is("abcdefg");
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.a_server_error_is_returned();
            AND.the_response_entity_is_an_error_respone();
            AND.the_error_message_is(MessageKey.AQWQAC14001E_error_selection_correct_classifier.getMessage().getFormattedMessage());
    }
    
    @Test
    public void test_two_classifier_services_with_specified_name() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
            AND.a_second_classifier_service_is_mocked();
            AND.configured_classifier_name_is(SERVICE_NAME);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(0);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_get_top_questions() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            AND.the_conversation_api_is_created();
        WHEN.user_requests_top_questions();
        THEN.the_response_status_code_is_ok();
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(TopQuestionsResponse.class);
    }
    
    @Test
    public void test_high_conf_answer_list_is_trimmed() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.88);
                AND.answer("example answer 2", 0.02);
                AND.answer("example answer 3", 0.02);
                AND.answer("example answer 4", 0.02);
                AND.answer("example answer 5", 0.02);
                AND.answer("example answer 6", 0.02);
                AND.answer("example answer 7", 0.02);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(ConversationApiImpl.HIGH_CONF_ANSWER_COUNT);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.88, ConfidenceCategoryEnum.HIGH);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_low_conf_answer_list_is_trimmed() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
                AND.answer("example answer 2", 0.11);
                AND.answer("example answer 3", 0.11);
                AND.answer("example answer 4", 0.11);
                AND.answer("example answer 5", 0.11);
                AND.answer("example answer 6", 0.11);
                AND.answer("example answer 7", 0.11);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question("What is the NL Classifier?");
        THEN. the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(ConversationApiImpl.LOW_CONF_ANSWER_COUNT);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);
            AND.verify_tracked_a_query("What is the NL Classifier?");
    }
    
    @Test
    public void test_ask_top_question_get_answer_from_store() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question_and_referrer(TOP_QUESTION.getQuestionText(), new MessageRequestReferrer("", SourceEnum.TOP_QUESTION));
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_get_answer_was_invoked_on_top_question_store(TOP_QUESTION.getQuestionText());
            AND.verify_the_response_number_of_answers_is(2);
                AND.verify_the_answer_list_contains_answer("Answer text for " + TOP_QUESTION.getQuestionText(), 1.00, ConfidenceCategoryEnum.HIGH);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);     
    }
    
    @Test
    public void test_ask_invalid_top_question() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question_and_referrer("This question isn't in the top question store", new MessageRequestReferrer("", SourceEnum.TOP_QUESTION));
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_get_answer_was_invoked_on_top_question_store("This question isn't in the top question store");
            AND.verify_the_response_number_of_answers_is(1);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);
    }
    
    @Test
    public void test_ask_refinement_question() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question_and_referrer(REFINEMENT_QUESTION.getQuestionText(), new MessageRequestReferrer("12345", SourceEnum.REFINEMENT));
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_get_was_invoked_on_response_cache(CONVERSATION_ID);
            AND.verify_the_response_number_of_answers_is(2);
                AND.verify_the_answer_list_contains_answer("Answer from mocked response cache for class: " + REFINEMENT_QUESTION.getClassName(), 1.00, ConfidenceCategoryEnum.HIGH);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);     
    }
    
    @Test
    public void test_ask_invalid_refinement_question_that_is_not_in_cache() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question_and_referrer("This question won't be in the response cache", new MessageRequestReferrer("12345", SourceEnum.REFINEMENT));
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_get_was_invoked_on_response_cache(CONVERSATION_ID);
            AND.verify_the_response_number_of_answers_is(1);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);     
    }
    
    @Test
    public void test_ask_question_with_no_referrer_answer_list_is_cached() throws Exception {
        GIVEN.the_classifier_service_is_mocked();
            WITH.an_available_classifier_instance();
            AND.the_classify_response_is_mocked();
                WITH.answer("example answer 1", 0.34);
            AND.the_answer_resolver_is_mocked(ConfidenceCategoryEnum.LOW);
            AND.the_conversation_api_is_created();
        WHEN.ask_question_is_invoked_with_question_and_referrer("This question should be cached", null);
        THEN.the_response_status_code_is_ok();
            AND.verify_classify_was_invoked_on(availableClassifier);
            AND.verify_the_response_is_not_null();
            AND.verify_the_response_is_a(MessageResponse.class);
            AND.verify_the_response_number_of_answers_is(1);
                AND.verify_the_answer_list_contains_answer("example answer 1", 0.34, ConfidenceCategoryEnum.LOW);
            AND.verify_put_was_invoked_on_response_cache(CONVERSATION_ID);
    }
    
    private void verify_put_was_invoked_on_response_cache(String conversationId) {
        verify(responseCache, times(1)).put(eq(CONVERSATION_ID), Matchers.anyListOf(Answer.class));
    }

    private void verify_get_was_invoked_on_response_cache(String conversationId) {
        verify(responseCache, times(1)).get(conversationId);
    }

    private void verify_get_answer_was_invoked_on_top_question_store(String question) throws QuestionNotFoundException, ResolutionException {
        verify(topQuestionsStore, times(1)).getAnswer(question);
    }

    private void configured_classifier_name_is(String value) {
        when(config.getProperty(ConfigurationConstants.CLASSIFIER_SERVICE_NAME)).thenReturn(value);
    }

    private void verify_the_answer_list_contains_answer(String answerText, double confidence, ConfidenceCategoryEnum confidenceCategory) {
        List<Answer> answers = ((MessageResponse) response.getEntity()).getResponses();
        for(Answer answer : answers) {
            if(answer.getText().equals(answerText) && answer.getConfidence().equals(confidence)) {
                assertNotNull(answer.getConfidenceCategory());
                assertEquals(confidenceCategory, answer.getConfidenceCategory()); // Our mock always returns this
                return;
            }
        }
        fail("Answer is missing from the response: ('" + answerText + "', '" + confidence + "')");
    }

    private void verify_the_response_number_of_answers_is(int i) {
        List<Answer> answers = ((MessageResponse) response.getEntity()).getResponses();
        assertNotNull(answers);
        assertEquals(i, answers.size());
    }

    private void verify_the_response_is_a(Class<?> cls) {
        assertThat(response.getEntity(), instanceOf(cls));
    }
    
    private void verify_the_response_is_not_null() {
        assertNotNull(response);
    }

    private void verify_classify_was_not_invoked_on(NLClassifier classifier) {
        ArgumentCaptor<String> questionCap = ArgumentCaptor.forClass(String.class);
        verify(classifier, times(0)).classify(questionCap.capture());
    }
    
    private void verify_classify_was_invoked_on(NLClassifier classifier) {
        ArgumentCaptor<String> questionCap = ArgumentCaptor.forClass(String.class);
        verify(classifier, times(1)).classify(questionCap.capture());
    }
    
    private void the_response_status_code_is_ok() {
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }
    
    private void the_error_message_is(String errorMsg) {
        assertEquals(errorMsg, ((ErrorResponse)response.getEntity()).getMessage());
    }
    
    private void the_response_entity_is_an_error_respone() {
        assertTrue(response.getEntity() instanceof ErrorResponse);
    }

    private void a_server_error_is_returned() {
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
    
    private void ask_question_is_invoked_with_question_and_referrer(String question, MessageRequestReferrer referrer) throws NotFoundException {
        response = api.askQuestion(CONVERSATION_ID, new MessageRequest(question, referrer));
    }

    private void ask_question_is_invoked_with_question(String question) throws NotFoundException {
        response = api.askQuestion(CONVERSATION_ID, new MessageRequest(question, null));
    }
    
    private void user_requests_top_questions() throws NotFoundException {
        response = api.getTopQuestions(CONVERSATION_ID);
    }

    private void the_conversation_api_is_created() {
        api = new ConversationApiImpl(services, resolver, tracking, config, topQuestionsStore, conversationManager, categorizer, responseCache);
    }

    private void the_answer_resolver_is_mocked(final ConfidenceCategoryEnum defaultConfidenceCategory) throws ResolutionException {
        resolver = mock(AnswerResolver.class);
        when(resolver.resolve(any(NLClassifiedClass.class))).thenAnswer(new org.mockito.stubbing.Answer<Answer>() {
            @Override
            public Answer answer(InvocationOnMock invocation) throws Throwable {
                NLClassifiedClass classifiedClass = (NLClassifiedClass) invocation.getArguments()[0];
                Answer a = new Answer();
                a.setText(classifiedClass.getClassName());
                a.setClassName(classifiedClass.getClassName());
                a.setConfidence(classifiedClass.getConfidence());
                a.setConfidenceCategory(defaultConfidenceCategory);
                return a;
            }
        });
    }
    
    private void answer(String className, double confidence) {
        classes.add(new NLClassifiedClass(className, confidence));
    }
    
    private void the_classify_response_is_mocked() {
        classes = new ArrayList<NLClassifiedClass>();
        when(classifyResponse.getClasses()).thenReturn(classes);
    }
    
    private void an_available_classifier_instance() {
        availableClassifier = mock(NLClassifier.class);
        when(availableClassifier.getStatus()).thenReturn(com.ibm.watson.app.common.services.nlclassifier.NLClassifier.Status.AVAILABLE);
        when(availableClassifier.classify(any(String.class))).thenReturn(classifyResponse);
        classifiers.add(availableClassifier);
    }
    
    private void a_training_classifier_instance() {
        trainingClassifier = mock(NLClassifier.class);
        when(trainingClassifier.getStatus()).thenReturn(com.ibm.watson.app.common.services.nlclassifier.NLClassifier.Status.TRAINING);
        classifiers.add(trainingClassifier);
    }
    
    private void the_classifier_service_is_mocked(String serviceName) {
        NLClassifierService service = mock(NLClassifierService.class);
        classifiers = new ArrayList<NLClassifier>();
        when(service.getClassifiers()).thenReturn(classifiers);
        when(service.getName()).thenReturn(serviceName);
        
        if (services == null) {
            services = new HashSet<NLClassifierService>();
        }
        services.add(service);
    }
    
    private void the_classifier_service_is_mocked() {
        the_classifier_service_is_mocked(SERVICE_NAME);
    }
    
    private void a_second_classifier_service_is_mocked() {
        the_classifier_service_is_mocked(SERVICE_NAME_2);
    }

    private void the_classifier_service_is_empty() {
        services = new HashSet<NLClassifierService>();
    }
    
    private void config_is_mocked() {
        config = mock(ConfigurationService.class);
    }
    
    private void user_tracking_is_mocked() {
        tracking = mock(UserTracking.class);
    }
    
    private void categorizer_is_mocked() {
        categorizer = mock(ConfidenceCategorizer.class);        
        when(categorizer.categorize(Matchers.anyListOf(Answer.class))).thenAnswer(new org.mockito.stubbing.Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }            
        });
    }
    
    private void response_cache_is_mocked() {
        responseCache = mock(ResponseCache.class);
        when(responseCache.get(anyString())).thenAnswer(new org.mockito.stubbing.Answer<List<Answer>>() {
            @Override
            public List<Answer> answer(InvocationOnMock invocation) throws Throwable {
                String conversationId = invocation.getArgumentAt(0, String.class);
                if(conversationId.equals(CONVERSATION_ID)) {
                    List<Answer> answers = new ArrayList<>();
                    Answer a = new Answer();
                    a.setText("Answer from mocked response cache for class: " + REFINEMENT_QUESTION.getClassName());
                    a.setCanonicalQuestion(REFINEMENT_QUESTION.getQuestionText());
                    a.setClassName(REFINEMENT_QUESTION.getClassName());
                    answers.add(a);
                    return answers;
                }
                return Collections.emptyList();
            }
        });
    }
    
    private void top_questions_store_is_mocked() throws Exception {
        topQuestionsStore = mock(TopQuestionsStore.class);
        List<Question> topQs = new ArrayList<>();
        topQs.add(TOP_QUESTION);
        when(topQuestionsStore.getTopQuestions()).thenReturn(topQs);
                
        when(topQuestionsStore.getAnswer(anyString())).thenAnswer(new org.mockito.stubbing.Answer<Answer>() {
            @Override
            public Answer answer(InvocationOnMock invocation) throws Throwable {
                String question = invocation.getArgumentAt(0, String.class);
                if(question.equals(TOP_QUESTION.getQuestionText())) {
                    Answer topQuestionAnswer = new Answer();
                    topQuestionAnswer.setClassName(TOP_QUESTION.getClassName());
                    topQuestionAnswer.setText("Answer text for " + TOP_QUESTION.getQuestionText());
                    return topQuestionAnswer;
                }
                throw new QuestionNotFoundException("No answer declared in stubbed impl for question: " + question);
            }            
        });
    }
    
    private void the_conversation_manager_is_mocked() {
        conversationManager = mock(ConversationManager.class);
        when(conversationManager.getNewConversationId()).thenReturn(CONVERSATION_ID);
    }
    
    @SuppressWarnings("unchecked")
    private void verify_tracked_a_query(String questionText) {
        verify(tracking).questionAsked(anyString(), anyString(), eq(questionText), any(InputMode.class), any(List.class));
    }

    private final ConversationApiImplTest GIVEN = this, WHEN = this, THEN = this, WITH = this, AND = this;
}
