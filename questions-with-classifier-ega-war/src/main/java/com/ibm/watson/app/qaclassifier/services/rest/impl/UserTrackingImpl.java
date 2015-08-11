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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.message.Message;

import com.google.inject.Inject;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.rest.model.Question;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger.Location;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger.Provenance;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class UserTrackingImpl implements UserTracking {

    private static final String hostname;

    private final UserTrackingLogger trackingLogger;

    static {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        	Message msg = MessageKey.AQWQAC24004E_unable_determine_hostname.getMessage();
            throw new RuntimeException(msg.getFormattedMessage(), e);
        }
    }

    @Inject
    public UserTrackingImpl(UserTrackingLogger trackingLogger) {
        this.trackingLogger = trackingLogger;
    }

    @Override
    public String questionAsked(String conversationId, String messageId, String questionText, InputMode mode, List<Answer> answers) {
        Date timestamp = new Date();

        Long referral = null;

        if (mode.equals(InputMode.CLICKED)) {
            if (messageId != null) {
                // Refinement question
                long impressionId = trackingLogger.getImpressionIdForMessage(Long.valueOf(messageId), questionText);
                referral = trackingLogger.click(timestamp, conversationId, hostname, impressionId);
            } else {
                // Top question click
                long impressionId = trackingLogger.getTopQuestionImpressionId(Long.valueOf(conversationId), questionText);
                referral = trackingLogger.click(timestamp, conversationId, hostname, impressionId);
            }
        }

        Long queryId = trackingLogger.query(timestamp, conversationId, hostname, referral, questionText, mode);

        if (answers.isEmpty()) {
            questionNotAnswered(conversationId, queryId, timestamp);
        } else if (answers.get(0).getConfidenceCategory().equals(ConfidenceCategoryEnum.HIGH)) {
            questionAnsweredWithHighConfidence(conversationId, queryId, answers, timestamp);
        } else {
            questionAnsweredWithLowConfidence(conversationId, queryId, answers, timestamp);
        }

        return queryId.toString();
    }

    @Override
    public void answerWasHelpful(String conversationId, String referringMessageId) {
        trackingLogger.click(new Date(), conversationId, hostname,
                trackingLogger.getPositiveImpressionIdForMessage(Long.valueOf(referringMessageId)));
    }

    @Override
    public void answerWasUnhelpful(String conversationId, String referringMessageId) {
        Date timestamp = new Date();

        trackingLogger.click(timestamp, conversationId, hostname,
                trackingLogger.getNegativeImpressionIdForMessage(Long.valueOf(referringMessageId)));

        List<Long> hiddenImpressionIds = trackingLogger.getHiddenImpressionIdsForMessage(Long.valueOf(referringMessageId));
        // The top answer was already shown to the user.
        // Now we show the canonical question for each of the other answers.
        for (long impressionId : hiddenImpressionIds) {
            trackingLogger.unhideImpression(impressionId, Location.MAIN_RESULTS_AREA);
        }

        // Offset is after top answer, positive feedback, negative feedback, and unhidden impressions
        int offset = 3 + hiddenImpressionIds.size();

        trackingLogger.impression(timestamp, conversationId, hostname, Long.valueOf(referringMessageId), VISIT_THE_FORUM,
                null, offset, null, Location.MAIN_RESULTS_AREA, Provenance.ALGO, 0);
    }

    @Override
    public void questionRefinementsWereUnhelpful(String conversationId, String messageId) {
        Date timestamp = new Date();
        Long impressionId = trackingLogger.getNoneOfTheAboveImpressionIdForMessage(Long.valueOf(messageId));
        trackingLogger.click(timestamp, conversationId, hostname, impressionId);
        trackingLogger.impression(timestamp, conversationId, hostname, Long.valueOf(messageId), VISIT_THE_FORUM, null,
                1, null, Location.MAIN_RESULTS_AREA, Provenance.ALGO, 0);
    }

    @Override
    public void visitedTheForum(String conversationId, String messageId) {
        Long impressionId = trackingLogger.getForumVisitImpressionIdForMessage(Long.valueOf(messageId));
        trackingLogger.click(new Date(), conversationId, hostname, impressionId);
    }

    @Override
    public void displayedTopQuestions(String conversationId, List<Question> topQuestions) {
        logTopQuestionImpressions(conversationId, topQuestions, Location.RIGHT_RAIL);
    }

    @Override
    public void redisplayedTopQuestions(String conversationId, List<Question> topQuestions) {
        logTopQuestionImpressions(conversationId, topQuestions, Location.MAIN_RESULTS_AREA);
    }

    private void logTopQuestionImpressions(String conversationId, List<Question> topQuestions, Location location) {
        Date timestamp = new Date();
        for (int i = 0; i < topQuestions.size(); i++) {
            trackingLogger.impression(timestamp, conversationId, hostname, null, topQuestions.get(i).getQuestionText(), null, i + 1, null, location, Provenance.ALGO, 0);
        }
    }

    private void questionAnsweredWithHighConfidence(String conversationId, long referringQueryId, List<Answer> answers, Date timestamp) {
        if (!answers.isEmpty()) {
            Answer topAnswer = answers.get(0);
            trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId, topAnswer.getText(),
                    topAnswer.getClassName(), 1, null, Location.MAIN_RESULTS_AREA, Provenance.INLINE, topAnswer.getConfidence());
            trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId, THIS_WAS_HELPFUL,
                    null, 2, null, Location.FOOTER, Provenance.ALGO, 0);
            trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId, I_STILL_NEED_HELP,
                    null, 3, null, Location.FOOTER, Provenance.ALGO, 0);
        }
        if (answers.size() > 1) {
            for (int i = 1; i < answers.size(); i++) {
                Answer answer = answers.get(i);
                trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId,
                        answer.getCanonicalQuestion(), answer.getClassName(), 3 + i, null, Location.HIDDEN,
                        Provenance.ALGO, answer.getConfidence());
            }
        }
    }

    private void questionAnsweredWithLowConfidence(String conversationId, long referringQueryId, List<Answer> answers, Date timestamp) {
        for (int i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId, answer.getCanonicalQuestion(),
                    answer.getClassName(), i + 1, null, Location.MAIN_RESULTS_AREA, Provenance.ALGO, answer.getConfidence());
        }
        trackingLogger.impression(timestamp, conversationId, hostname, referringQueryId, NONE_OF_THE_ABOVE,
                null, answers.size(), null, Location.MAIN_RESULTS_AREA, Provenance.ALGO, 0);
    }
    
    private void questionNotAnswered(String conversationId, long referringQueryId, Date timestamp) {
        trackingLogger.impression(timestamp, conversationId, hostname, Long.valueOf(referringQueryId), VISIT_THE_FORUM,
                null, 1, null, Location.MAIN_RESULTS_AREA, Provenance.ALGO, 0);
    }
}
