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

package com.ibm.watson.app.qaclassifier.rest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.TestRule;

import com.ibm.watson.app.qaclassifier.SampleQuestions;
import com.ibm.watson.app.qaclassifier.rest.model.Feedback;
import com.ibm.watson.app.qaclassifier.rest.model.Feedback.ActionEnum;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequest;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer.SourceEnum;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingEvent;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingEvent.TypeEnum;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingResponse;
import com.ibm.watson.app.qaclassifier.services.AbstractUserTrackingIT;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;

public class UserTrackingApiIT extends AbstractUserTrackingIT {

    @ClassRule
    public static TestRule restAssuredManager = new RestAssuredManager();

    private String conversationId;
    private String messageId;
    private List<String> answers;
    private List<String> topQuestions;
    private List<String> canonicalQuestions;

    @Before
    public void startConversation() {
        JsonPath json = post("/api/v1/conversation").then().statusCode(200).and().extract().jsonPath();
        conversationId = json.getString("conversationId");
        topQuestions = json.getList("topQuestions.questionText");
    }

    @Override
    protected void user_clicks_this_is_helpful() {
        given().contentType(ContentType.JSON)
                .body(new Feedback(conversationId, messageId, ActionEnum.HELPFUL))
                .post("/api/v1/feedback")
                .then().statusCode(204);
    }

    @Override
    protected void user_clicks_none_of_the_above() {
        given().contentType(ContentType.JSON)
                .body(new Feedback(conversationId, messageId, ActionEnum.NO_HELPFUL_REFINEMENTS))
                .post("/api/v1/feedback")
                .then().statusCode(204);
    }

    @Override
    protected void user_clicks_I_still_need_help() {
        given().contentType(ContentType.JSON)
                .body(new Feedback(conversationId, messageId, ActionEnum.UNHELPFUL))
                .post("/api/v1/feedback")
                .then().statusCode(204);
    }

    @Override
    protected void user_clicks_visit_the_forum() {
        given().contentType(ContentType.JSON)
                .body(new Feedback(conversationId, messageId, ActionEnum.FORUM_REDIRECT))
                .post("/api/v1/feedback")
                .then().statusCode(204);
    }

    @Override
    protected void a_question_is_answered_with_high_confidence() {
        JsonPath json = given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(SampleQuestions.HIGH_CONFIDENCE, null))
                .post("/api/v1/conversation/{conversationId}")
                .then().statusCode(200)
                .and().body("responses", hasSize(greaterThan(0)))
                .and().body("responses[0].confidenceCategory", equalTo("HIGH"))
                .and().extract().jsonPath();

        messageId = json.getString("messageId");
        answers = json.getList("responses.text");
        canonicalQuestions = json.getList("responses.canonicalQuestion");
    }

    @Override
    protected void a_question_is_answered_with_low_confidence() {
        JsonPath json = given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(SampleQuestions.LOW_CONFIDENCE, null))
                .post("/api/v1/conversation/{conversationId}")
                .then().statusCode(200)
                .and().body("responses", hasSize(greaterThan(0)))
                .and().body("responses[0].confidenceCategory", equalTo("LOW"))
                .and().extract().jsonPath();

        messageId = json.getString("messageId");
        answers = json.getList("responses.text");
        canonicalQuestions = json.getList("responses.canonicalQuestion");
    }

    @Override
    protected void a_question_is_not_answered() {
        JsonPath json = given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(SampleQuestions.NO_ANSWERS, null))
                .post("/api/v1/conversation/{conversationId}")
                .then().statusCode(200)
                .and().body("responses", emptyIterable())
                .and().body("messageId", not(isEmptyOrNullString()))
                .and().extract().jsonPath();

        messageId = json.getString("messageId");
        answers = Collections.emptyList();
        canonicalQuestions = Collections.emptyList();
    }

    @Override
    protected void a_question_is_clicked() {
        JsonPath json = given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(topQuestions.get(0), new MessageRequestReferrer(null, SourceEnum.TOP_QUESTION)))
                .post("/api/v1/conversation/{conversationId}")
                .then().statusCode(200)
                .and().body("responses", hasSize(greaterThan(0)))
                .and().extract().jsonPath();

        messageId = json.getString("messageId");
        answers = json.getList("responses.text");
    }

    @Override
    protected void a_question_is_typed() {
        a_question_is_answered_with_high_confidence();
    }

    @Override
    protected void user_clicks_a_refinement_question() {
        given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(canonicalQuestions.get(1), new MessageRequestReferrer(messageId, SourceEnum.REFINEMENT)))
                .post("/api/v1/conversation/{conversationId}")
                .then().statusCode(200)
                .and().body("responses", hasSize(greaterThan(0)));
    }

    @Override
    protected void the_positive_feedback_is_logged() {
        feedback_is_logged(UserTracking.THIS_WAS_HELPFUL);
    }

    @Override
    protected void the_negative_feedback_is_logged() {
        feedback_is_logged(UserTracking.I_STILL_NEED_HELP);
    }

    @Override
    protected void the_forum_visit_is_logged() {
        feedback_is_logged(UserTracking.VISIT_THE_FORUM);
    }

    @Override
    protected void the_top_answer_text_is_logged() {
       getFirstImpression(getTrackingEvents(), answers.get(0));
    }

    @Override
    protected void the_feedback_buttons_are_logged() {
        List<TrackingEvent> events = getTrackingEvents();

        getFirstImpression(events, UserTracking.THIS_WAS_HELPFUL);
        getFirstImpression(events, UserTracking.I_STILL_NEED_HELP);
    }

    @Override
    protected void the_canonical_questions_are_logged() {
        List<TrackingEvent> events = getTrackingEvents();

        for (String canonicalQuestion : canonicalQuestions) {
            getFirstImpression(events, canonicalQuestion);
        }

    }

    @Override
    protected void none_of_the_above_is_logged() {
        getFirstImpression(getTrackingEvents(), UserTracking.NONE_OF_THE_ABOVE);
    }

    @Override
    protected void the_top_questions_are_displayed() {
        // No-op: top questions are displayed when conversation is started
    }

    @Override
    protected void the_top_questions_are_redisplayed() {
        given().pathParam("conversationId", conversationId)
                .get("/api/v1/conversation/{conversationId}/topQuestions")
                .then().statusCode(200);
    }

    @Override
    protected void the_recorded_mode_is(InputMode mode) {
        List<TrackingEvent> events = getTrackingEvents();
        assertThat("Wrong input mode for query in\n" + events, 
                getQueries(events).get(0).getArgs().get("mode"), equalTo(mode.toString()));
    }

    @Override
    protected void two_queries_are_logged_with_no_referral() {
        List<TrackingEvent> queries = new ArrayList<>();
        List<TrackingEvent> events = getTrackingEvents();
        for (TrackingEvent event : events) {
            if (event.getType().equals(TypeEnum.QUERY)) {
                queries.add(event);
            }
        }

        assertThat("Expected to find two queries in\n" + events, queries, hasSize(2));
        assertThat(queries.get(0) + "\nshould not have a referral", queries.get(0).getReferral(), nullValue());
        assertThat(queries.get(1) + "\nshould not have a referral", queries.get(1).getReferral(), nullValue());
    }

    @Override
    protected void the_refinement_click_is_logged() {
        List<TrackingEvent> events = getTrackingEvents();
        TrackingEvent impression = getFirstImpression(events, canonicalQuestions.get(1));
        List<TrackingEvent> clicks = getClicks(events);
        TrackingEvent click = clicks.get(clicks.size() - 1);

        assertThat("Incorrect referrer for\n" + click + "\nin\n" + events,
                click.getReferral(), equalTo(impression.getId()));
    }

    @Override
    protected void the_refinement_query_has_a_referral() {
        List<TrackingEvent> events = getTrackingEvents();
        List<TrackingEvent> queries = getQueries(events);
        TrackingEvent query = queries.get(queries.size() - 1);
        assertThat("Referral for\n" + query + "\nin\n" + events + " should not be null",
                query.getReferral(), notNullValue());
    }


    @Override
    protected void the_two_questions_have_different_referrers() {
        List<TrackingEvent> queries = new ArrayList<>();
        List<TrackingEvent> events = getTrackingEvents();

        for (TrackingEvent event : events) {
            if (event.getType().equals(TypeEnum.QUERY)) {
                queries.add(event);
            }
        }

        assertThat("Expected to find two queries in\n" + events, queries, hasSize(2));
        assertThat(queries.get(0) + "\nshould have a referral", queries.get(0).getReferral(), notNullValue());
        assertThat(queries.get(1) + "\nshould have a referral", queries.get(1).getReferral(), notNullValue());
        assertThat(queries.get(0) + "\nand\n" + queries.get(1) + "\nshould have different referrals",
                queries.get(0).getReferral(), not(equalTo(queries.get(1).getReferral())));
    }

    private List<TrackingEvent> getTrackingEvents() {
        return given().param("conversationId", conversationId)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(200)
                .and().extract().as(TrackingResponse.class).getEvents();
    }

    private TrackingEvent getFirstImpression(List<TrackingEvent> events, String prompt) {
        TrackingEvent impression = null;
        for (TrackingEvent event : events) {
            if (event.getType().equals(TrackingEvent.TypeEnum.IMPRESSION) && event.getArgs().get("prompt").equals(prompt)) {
                impression = event;
                break;
            }
        }
        assertThat("Expected to find an impression with prompt:\n" + prompt + "\nin\n" + events,
                impression, notNullValue());
        return impression;
    }

    private List<TrackingEvent> getClicks(List<TrackingEvent> events) {
        List<TrackingEvent> clicks = new ArrayList<>();
        for (TrackingEvent event : events) {
            if (event.getType().equals(TrackingEvent.TypeEnum.CLICK)) {
                clicks.add(event);
            }
        }
        assertThat("Expected to find a click in\n" + events, clicks, hasSize(greaterThan(0)));
        return clicks;
    }

    private List<TrackingEvent> getQueries(List<TrackingEvent> events) {
        List<TrackingEvent> queries = new ArrayList<>();
        for (TrackingEvent event : events) {
            if (event.getType().equals(TrackingEvent.TypeEnum.QUERY)) {
                queries.add(event);
            }
        }
        assertThat("Expected to find a query in\n" + events, queries, hasSize(greaterThan(0)));
        return queries;
    }

    private void feedback_is_logged(String impressionPrompt) {
        List<TrackingEvent> events = getTrackingEvents();
        TrackingEvent impression = getFirstImpression(events, impressionPrompt);
        List<TrackingEvent> clicks = getClicks(events);
        TrackingEvent click = clicks.get(clicks.size() - 1);

        assertThat("Incorrect referrer for\n" + click + "\nin\n" + events, 
                click.getReferral(), equalTo(impression.getId()));
    }
}
