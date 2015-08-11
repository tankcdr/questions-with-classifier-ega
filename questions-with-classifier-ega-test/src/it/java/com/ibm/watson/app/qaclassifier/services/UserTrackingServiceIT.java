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

package com.ibm.watson.app.qaclassifier.services;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.common.persistence.jpa.impl.ApplicationPersistenceModule;
import com.ibm.watson.app.qaclassifier.rest.ClassifierRestModule;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.TypeEnum;
import com.ibm.watson.app.qaclassifier.rest.model.Question;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ClickEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ImpressionEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.QueryEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.TrackingEventEntity;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;
import com.ibm.watson.app.qaclassifier.services.rest.impl.DatabaseConversationManager;
import com.ibm.watson.app.qaclassifier.services.rest.impl.UserTrackingDBLogger;
import com.ibm.watson.app.qaclassifier.services.rest.impl.UserTrackingImpl;

// Suppress warnings from Hamcrest matchers
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserTrackingServiceIT extends AbstractUserTrackingIT {

    private static Injector injector;
    private static PersistenceEntityProvider provider;

    private List<Answer> answers;
    private List<String> messageIds = new ArrayList<>();

    private UserTracking tracking;
    private UserTrackingDBLogger logger;
    private String conversationId;

    

    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(
                new ClassifierRestModule(),
                new ClassifierServicesModule(),
                new ApplicationPersistenceModule("com.ibm.watson.app.qaclassifier.db.itest"));
        provider = injector.getInstance(PersistenceEntityProvider.class);
    }

    @Before
    public void initializeTracker() {
        logger = new UserTrackingDBLogger(provider);
        tracking = new UserTrackingImpl(logger);
        conversationId = new DatabaseConversationManager(provider).getNewConversationId();
    }

    /**
     * Finds an impression of the given query that matches the given matcher.
     * Raises an AssertionError if the impression cannot be found.
     * 
     * @param query
     * @param matcher
     * @return the matching impression
     */
    private static ImpressionEntity findImpression(QueryEntity query, final Matcher<ImpressionEntity> matcher) {
        assertThat("Could not find expected impression for " + query, query.getImpressions(), hasItem(matcher));
        Iterable<ImpressionEntity> matches = Iterables.filter(query.getImpressions(),
                new Predicate<ImpressionEntity>() {
                    @Override
                    public boolean apply(ImpressionEntity input) {
                        return matcher.matches(input);
                    }
                });
        assertThat("Did not find exactly one matching impression for " + query + ": " + matches,
                matches, (Matcher) iterableWithSize(1));
        return matches.iterator().next();
    }

    private static QueryEntity getQuery(String messageId) {
        assertNotNull("messageId is null", messageId);
        QueryEntity q = provider.getEntityManager().find(QueryEntity.class, messageId);
        assertThat("No tracked query found for messageId=" + messageId, q, notNullValue());
        return q;
    }

    private static Matcher<ImpressionEntity> hasPrompt(final String prompt) {
        return new FeatureMatcher<ImpressionEntity, String>(equalTo(prompt), "Impression prompt", "Impression prompt") {
            @Override
            protected String featureValueOf(ImpressionEntity actual) {
                return actual.getPrompt();
            }
        };
    }

    private static Matcher<TrackingEventEntity> hasReferral() {
        return new FeatureMatcher<TrackingEventEntity, TrackingEventEntity>(notNullValue(), "referral", "referral") {
            @Override
            protected TrackingEventEntity featureValueOf(TrackingEventEntity actual) {
                return actual.getReferral();
            }
        };
    }

    protected void the_two_questions_have_different_referrers() {
        QueryEntity firstQueryEntity = getQuery(messageIds.get(0));
        QueryEntity secondQueryEntity = getQuery(messageIds.get(1));

        assertNotNull("First query is missing referral click", firstQueryEntity.getReferral());
        assertNotNull("Second query is missing referral click", secondQueryEntity.getReferral());

        assertNotNull("First query click is missing referral impression", firstQueryEntity.getReferral().getReferral());
        assertNotNull("First query click is missing referral impression", secondQueryEntity.getReferral().getReferral());

        assertThat("Query referrers should not be the same click", 
                firstQueryEntity.getReferral().getId(), 
                not(equalTo(secondQueryEntity.getReferral().getId())));
        assertThat("Query click referrers should not be the same impression",
                firstQueryEntity.getReferral().getReferral().getId(), 
                not(equalTo(secondQueryEntity.getReferral().getReferral().getId())));
    }

    protected void the_top_questions_are_displayed() {
        List<Question> topQuestions = new ArrayList<>();
        topQuestions.add(new Question("Top question 1", null));
        topQuestions.add(new Question("What is the classifier?", null));
        tracking.displayedTopQuestions(conversationId, topQuestions);
    }

    protected void the_top_questions_are_redisplayed() {
        List<Question> topQuestions = new ArrayList<>();
        topQuestions.add(new Question("Top question 1", null));
        topQuestions.add(new Question("What is the classifier?", null));
        tracking.redisplayedTopQuestions(conversationId, topQuestions);
    }

    protected void a_question_is_answered_with_high_confidence() {
        answers = highConfAnswers();
        messageIds.add(tracking.questionAsked(conversationId, null, "What is the classifier?", InputMode.TYPED, answers));
    }

    protected void a_question_is_answered_with_low_confidence() {
        answers = lowConfAnswers();
        messageIds.add(tracking.questionAsked(conversationId, null, "What is the classifier?", InputMode.TYPED, answers));
    }

    @Override
    protected void a_question_is_not_answered() {
        answers = Collections.emptyList();
        messageIds.add(tracking.questionAsked(conversationId, null, "What is the classifier?", InputMode.TYPED, answers));
    }

    protected void a_question_is_clicked() {
        answers = highConfAnswers();
        messageIds.add(tracking.questionAsked(conversationId, null, "What is the classifier?", InputMode.CLICKED, answers));
    }

    protected void a_question_is_typed() {
        answers = highConfAnswers();
        messageIds.add(tracking.questionAsked(conversationId, null, "What is the classifier?", InputMode.TYPED, answers));
    }

    private Answer answer(int index, double confidence) {
        return new Answer(TypeEnum.TEXT, "Answer " + index + " text", confidence,
                confidence > 0.5 ? ConfidenceCategoryEnum.HIGH : ConfidenceCategoryEnum.LOW,
                "Canonical question " + index, "{class" + index + "}");
    }

    private String currentMessageId() {
        assertThat("", messageIds, not(empty()));
        return messageIds.get(messageIds.size() - 1);
    }

    private QueryEntity getQuery() {
        return getQuery(currentMessageId());
    }

    private List<Answer> highConfAnswers() {
        return Arrays.asList(answer(1, 0.9), answer(2, 0.6), answer(3, 0.2));
    }

    private List<Answer> lowConfAnswers() {
        return Arrays.asList(answer(1, 0.4), answer(2, 0.3), answer(3, 0.2));
    }

    protected void none_of_the_above_is_logged() {
        QueryEntity q = getQuery();
        assertThat("No none of the above impression for " + q,
                q.getImpressions(), hasItem(hasPrompt(UserTracking.NONE_OF_THE_ABOVE)));
    }

    protected void the_canonical_questions_are_logged() {
        QueryEntity q = getQuery();
        for (Answer lowConfAnswer : answers) {
            assertThat("No impresson for answer " + lowConfAnswer + " for query " + q,
                    q.getImpressions(), hasItem(hasPrompt(lowConfAnswer.getCanonicalQuestion())));
        }
    }

    protected void the_feedback_buttons_are_logged() {
        QueryEntity q = getQuery();
        assertThat(q.getImpressions(), hasItem(hasPrompt(UserTracking.THIS_WAS_HELPFUL)));
        assertThat(q.getImpressions(), hasItem(hasPrompt(UserTracking.I_STILL_NEED_HELP)));
    }

    protected void the_forum_visit_is_logged() {
        findImpression(getQuery(), hasPrompt(UserTracking.VISIT_THE_FORUM));
    }

    protected void the_negative_feedback_is_logged() {
        ImpressionEntity negativeFeedback = findImpression(getQuery(), hasPrompt(UserTracking.I_STILL_NEED_HELP));
        assertThat("No click event tracked for " + negativeFeedback, negativeFeedback.getClick(), notNullValue());
    }

    protected void the_positive_feedback_is_logged() {
        ImpressionEntity positiveFeedback = findImpression(getQuery(), hasPrompt(UserTracking.THIS_WAS_HELPFUL));
        assertThat("No click event tracked for " + positiveFeedback, positiveFeedback.getClick(), notNullValue());
    }

    protected void the_recorded_mode_is(InputMode typed) {
        assertThat("", getQuery().getMode(), equalTo(typed));
    }

    protected void the_refinement_click_is_logged() {
        ImpressionEntity negativeFeedback = findImpression(getQuery(messageIds.get(0)), hasPrompt(UserTracking.I_STILL_NEED_HELP));
        assertThat("", negativeFeedback.getClick(), allOf(notNullValue(), hasReferral()));
    }

    protected void the_refinement_query_has_a_referral() {
        QueryEntity followup = getQuery();
        ClickEntity click = followup.getReferral();
        assertThat("No referral click for " + followup, click, notNullValue());
        QueryEntity original = click.getReferral().getReferral();
        assertThat("No referral query for " + followup + " via " + click + " and " + click.getReferral(), original, notNullValue());
        assertThat("Wrong referral query for " + followup, click.getReferral().getReferral().getId(), is(getQuery(messageIds.get(0)).getId()));
        assertThat("Wrong input mode for " + followup, followup.getMode(), is(InputMode.CLICKED));
    }

    protected void the_top_answer_text_is_logged() {
        QueryEntity q = getQuery();
        assertThat(q.getImpressions(), hasItem(hasPrompt(answers.get(0).getText())));
        assertThat(q.getImpressions(), (Matcher) everyItem(hasReferral()));
    }

    protected void two_queries_are_logged_with_no_referral() {
        assertThat("Expect two messageIds", messageIds, (Matcher) iterableWithSize(2));
        assertThat(getQuery(messageIds.get(0)), not(hasReferral()));
        assertThat(getQuery(messageIds.get(1)), not(hasReferral()));
    }

    protected void user_clicks_a_refinement_question() {
        answers = highConfAnswers();
        messageIds.add(tracking.questionAsked(conversationId, currentMessageId(), answers.get(2).getCanonicalQuestion(), InputMode.CLICKED, answers));
    }

    protected void user_clicks_I_still_need_help() {
        tracking.answerWasUnhelpful(conversationId, currentMessageId());
    }

    protected void user_clicks_none_of_the_above() {
        tracking.questionRefinementsWereUnhelpful(conversationId, currentMessageId());
    }

    protected void user_clicks_this_is_helpful() {
        tracking.answerWasHelpful(conversationId, currentMessageId());
    }

    protected void user_clicks_visit_the_forum() {
        tracking.visitedTheForum(conversationId, currentMessageId());
    }
}
