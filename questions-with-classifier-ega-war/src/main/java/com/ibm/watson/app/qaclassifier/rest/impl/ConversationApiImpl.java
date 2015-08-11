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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import com.google.inject.Inject;
import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifier;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifier.Status;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifierService;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.ConfigurationConstants;
import com.ibm.watson.app.qaclassifier.rest.ConversationApiInterface;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.rest.model.Greeting;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequest;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer.SourceEnum;
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

public class ConversationApiImpl extends AbstractRestApiImpl implements ConversationApiInterface, ConfigurationConstants {
    private static final Logger logger = LogManager.getLogger();

    public static final int HIGH_CONF_ANSWER_COUNT = 6;
    public static final int LOW_CONF_ANSWER_COUNT = HIGH_CONF_ANSWER_COUNT - 1;

    private final NLClassifierService service;
    private final AnswerResolver resolver;
    private final UserTracking tracking;
    private final TopQuestionsStore topQuestionsStore;
    private final ConversationManager conversationManager;
    private final ConfidenceCategorizer categorizer;
    private final ResponseCache responseCache;
    
    @Inject
    public ConversationApiImpl(Set<NLClassifierService> services, AnswerResolver resolver, UserTracking tracking, 
            ConfigurationService config, TopQuestionsStore topQuestionsStore, ConversationManager conversationManager, ConfidenceCategorizer categorizer, ResponseCache responseCache) {
        this.service = findClassifierService(services, config);
        this.resolver = resolver;
        this.tracking = tracking;
        this.topQuestionsStore = topQuestionsStore;
        this.conversationManager = conversationManager;
        this.categorizer = categorizer;
        this.responseCache = responseCache;
    }

    private NLClassifierService findClassifierService(Set<NLClassifierService> services, ConfigurationService config) {
        if (services.isEmpty()) {
            logger.warn(MessageKey.AQWQAC12001W_no_classifier_service.getMessage());
            return null;
        }
        
        // If there is only one, just take it
        if (services.size() == 1) {
            NLClassifierService service = services.iterator().next();
            logger.info(MessageKey.AQWQAC10101I_using_classifier_service_1.getMessage(service.getName()));
            return service;
        }
        
        // Find the correct classifier service to use
        final String classifierServiceName = config.getProperty(CLASSIFIER_SERVICE_NAME);
        if (classifierServiceName == null) {
        	logger.warn(MessageKey.AQWQAC12002W_missing_required_conf_parameter_classification_unavail_1.getMessage(CLASSIFIER_SERVICE_NAME));
            return null;
        }
        
        Iterator<NLClassifierService> iter = services.iterator();
        while (iter.hasNext()) {
            NLClassifierService potentialService = iter.next();
            if(potentialService.getName().equals(classifierServiceName)) {
                logger.info(MessageKey.AQWQAC10101I_using_classifier_service_1.getMessage(potentialService.getName()));
                return potentialService;
            }
        }
        logger.error(MessageKey.AQWQAC14000E_could_not_find_classifier_service_in_conf_classification_unavail_1.getMessage(classifierServiceName));
        return null;
    }

    @Override
    public Response askQuestion(String conversationId, MessageRequest message) throws NotFoundException {
        logger.entry(message.getMessage());
        
        if (message.getReferrer() != null) {
            if (message.getReferrer().getSource() == SourceEnum.REFINEMENT && message.getReferrer().getMessageId() == null) {
                return getBadRequestResponse("messageId required when referrer source is " + SourceEnum.REFINEMENT);
            }
            if (message.getReferrer().getSource() == null) {
                return getBadRequestResponse("referrer source required when referrer is set");
            }
        }
        
        if (service == null) {
            return error(MessageKey.AQWQAC14001E_error_selection_correct_classifier.getMessage());
        }

        List<NLClassifier> classifiers = service.getClassifiers();
        if (classifiers.isEmpty()) {
            return error(MessageKey.AQWQAC14002E_no_classifier_instances.getMessage());
        }

        NLClassifier classifier = null;
        for (NLClassifier potentialClassifier : classifiers) {
            if (potentialClassifier.getStatus().equals(Status.AVAILABLE)) {
                classifier = potentialClassifier;
                break;
            }
        }

        if (classifier == null) {
            return error(MessageKey.AQWQAC14003E_no_available_classifiers.getMessage());
        }

        NLClassiferClassifyResponse classifyResponse = classifier.classify(message.getMessage());

        List<Answer> answers = new ArrayList<Answer>();
        for (NLClassifiedClass classifiedClass : classifyResponse.getClasses()) {
            try {
                answers.add(resolver.resolve(classifiedClass));
            } catch (ResolutionException e) {
                logger.warn(MessageKey.AQWQAC12100W_could_not_resolve_answer_1.getMessage(e.getMessage()));
            }
        }
        
        List<Answer> previousAnswers = responseCache.get(conversationId);
        responseCache.put(conversationId, answers);
        
        // If there was no referrer, or if it's a top question, cache this response
        if(message.getReferrer() != null) {
            Answer knownAnswer = null;
            // This is either a refinement question (did you mean) or a top question, so force the correct answer to the top
            if(message.getReferrer().getSource() == SourceEnum.TOP_QUESTION) {
                try {
                    knownAnswer = topQuestionsStore.getAnswer(message.getMessage());
                } catch (QuestionNotFoundException | ResolutionException e) {
                    logger.warn(MessageKey.AQWQAC12003W_question_not_in_top_question_store.getMessage(), e); 
                }
            }
            if(message.getReferrer().getSource() == SourceEnum.REFINEMENT) {
                if(previousAnswers.size() > 0) {
                    // Find the refinement question in our previously cached list
                    for(Answer previousAnswer : previousAnswers) {
                        if(previousAnswer.getCanonicalQuestion().equals(message.getMessage())) {
                            knownAnswer = previousAnswer;
                            break;
                        }
                    }
                }
                if(knownAnswer == null) {
                    logger.warn(MessageKey.AQWQAC12004W_question_not_in_response_cache_1.getMessage(message.getMessage()));
                }
            }
            if(knownAnswer != null) {
                pushAnswerToTop(knownAnswer, answers);
            }
        }
        
        if(answers.size() > 0) {
            answers = categorizer.categorize(answers);
        }
        
        if (answers.size() > 0) {
            // Ensure that the classifier returned more than one class that we were able to resolve
            if (answers.get(0).getConfidenceCategory().equals(ConfidenceCategoryEnum.HIGH)) {
                answers.subList(Math.min(answers.size(), HIGH_CONF_ANSWER_COUNT), answers.size()).clear();
            } else {
                answers.subList(Math.min(answers.size(), LOW_CONF_ANSWER_COUNT), answers.size()).clear();
            }
        }

        String previousMessageId = null;
        if (message.getReferrer() != null) {
            // This is a refinement/suggestion to a high confidence incorrect or low confidence answer.
            // Keep track of the question/response that got us here.
            previousMessageId = message.getReferrer().getMessageId();
        }

        InputMode mode = message.getReferrer() == null ? InputMode.TYPED : InputMode.CLICKED;
        String messageId = tracking.questionAsked(conversationId, previousMessageId, message.getMessage(), mode, answers);

        return getOkResponse(new MessageResponse(messageId, message.getMessage(), answers));
    }

    @Override
    public Response getTopQuestions(String conversationId) throws NotFoundException {
        List<Question> topQuestions = topQuestionsStore.getTopQuestions();
        tracking.redisplayedTopQuestions(conversationId, topQuestions);
        return getOkResponse(new TopQuestionsResponse(topQuestions));
    }

    private Response error(Message msg) {
        logger.error(msg);
        return getErrorResponse(msg.getFormattedMessage());
    }

    @Override
    public Response startConversation() throws NotFoundException {
        String conversationId = conversationManager.getNewConversationId();
        List<Question> topQuestions = topQuestionsStore.getTopQuestions();
        tracking.displayedTopQuestions(conversationId, topQuestions);
        return getOkResponse(new Greeting(conversationId, topQuestions));
    }
    
    private void pushAnswerToTop(Answer topAnswer, List<Answer> answers) {
        // See if the top answer is already in the answer list. If it is, remove it
        Iterator<Answer> iter = answers.iterator();
        while(iter.hasNext()) {
            if(iter.next().getClassName().equals(topAnswer.getClassName())) {
                iter.remove();
            }
        }
        
        // Set this top answer as HIGH with 100% confidence
        topAnswer.setConfidence(1.00);
        topAnswer.setConfidenceCategory(ConfidenceCategoryEnum.HIGH);
        
        // Add this answer to the top
        answers.add(0, topAnswer);
    }
}
