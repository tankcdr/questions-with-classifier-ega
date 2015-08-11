/*
 * Copyright IBM Corp. 2015
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.watson.app.qaclassifier.rest.api;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;

import com.google.gson.Gson;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer.TypeEnum;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingEvent;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;

public class ManageApiIT {
    @ClassRule
    public static RestAssuredManager restManager = new RestAssuredManager();

    @Test
    public void test_get_and_remove_answer_from_database() throws InterruptedException {
        ensureAnswerDoesNotExist("someunknownclass");
        
        get("/api/v1/manage/answer/someunknownclass")
                .then().statusCode(404);

        // Maybe a better way to convert this to JSON
        ManagedAnswer answer = new ManagedAnswer();
        answer.setClassName("someunknownclass");
        answer.setType(TypeEnum.TEXT);
        answer.setText("The text of the unknown answer");
        answer.setCanonicalQuestion("The canonical question for the unknown answer");
        ManagedAnswer[] answers = {answer};
        String json = new Gson().toJson(answers);

        given()
                .contentType(ContentType.JSON)
                .request().body(json)
                .post("/api/v1/manage/answer")
                .then().statusCode(200);

        get("/api/v1/manage/answer/someunknownclass")
                .then().statusCode(200)
                .and().contentType(ContentType.JSON)
                .and().body("className", is("someunknownclass"))
                .and().body("type", is("TEXT"))
                .and().body("text", is("The text of the unknown answer"))
                .and().body("canonicalQuestion", is("The canonical question for the unknown answer"));

        delete("/api/v1/manage/answer/someunknownclass")
                .then().statusCode(200);
    }

    @Test
    public void test_get_and_remove_answer_with_long_text_from_database() throws InterruptedException {
        ensureAnswerDoesNotExist("longanswer");
        
        get("/api/v1/manage/answer/longanswer")
                .then().statusCode(404);

        // Just needs to be bigger than 255, currently 420
        final String answerText = StringUtils.repeat("This is not the answer you're looking for.", 10);

        // Maybe a better way to convert this to JSON
        ManagedAnswer answer = new ManagedAnswer();
        answer.setClassName("longanswer");
        answer.setType(TypeEnum.TEXT);
        answer.setText(answerText);
        answer.setCanonicalQuestion("The canonical question for the long answer");
        ManagedAnswer[] answers = {answer};
        String json = new Gson().toJson(answers);

        given()
                .contentType(ContentType.JSON)
                .request().body(json)
                .post("/api/v1/manage/answer")
                .then().statusCode(200);

        get("/api/v1/manage/answer/longanswer")
                .then().statusCode(200)
                .and().contentType(ContentType.JSON)
                .and().body("className", is("longanswer"))
                .and().body("type", is("TEXT"))
                .and().body("text", is(answerText))
                .and().body("canonicalQuestion", is("The canonical question for the long answer"));

        delete("/api/v1/manage/answer/longanswer")
                .then().statusCode(200);
    }

    @Test
    public void test_get_tracking_events_for_invalid_conversation() {
        given().param("conversationId", "-1")
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);
    }

    @Test
    public void test_get_tracking_events_for_valid_conversation() {
        JsonPath response = post("/api/v1/conversation").then().statusCode(200).and().extract().jsonPath();

        String conversationId = response.getString("conversationId");
        List<String> topQuestions = response.getList("topQuestions.questionText");

        assertThat("Bad conversation ID",
                conversationId, allOf(not(isEmptyOrNullString()), not("0")));
        assertThat("Didn't get any top questions",
                topQuestions, not(empty()));

        response = given().param("conversationId", conversationId)
                .when().get("/api/v1/manage/tracking")
                .then().contentType(ContentType.JSON)
                .and().statusCode(200)
                .and().extract().jsonPath();

        assertThat("All events logged as part of startConversation should be impressions",
                response.getList("events.type.unique()", String.class), contains(TrackingEvent.TypeEnum.IMPRESSION.toString()));
        assertThat("The top questions should be logged as impressions in order",
                response.getList("events.args.prompt"), contains(topQuestions.toArray()));
    }

    @Test
    public void test_get_all_tracking_events() {
        when().get("/api/v1/manage/tracking")
                .then().contentType(ContentType.JSON)
                .and().statusCode(200)
                .and().body("events", not(empty()));
    }

    @Test
    public void test_get_tracking_with_invalid_perPage_value() {
        given().param("perPage", -1)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);

        given().param("perPage", 0)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);

        given().param("perPage", 50001)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);
    }

    @Test
    public void test_get_tracking_with_invalid_page_value() {
        given().param("page", -1)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);

        given().param("page", 0)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(400);
    }

    @Test
    public void test_paged_tracking() {
        String conversationId = post("/api/v1/conversation").then().statusCode(200).and().extract().path("conversationId");

        int numEvents = given().param("conversationId", conversationId)
                .when().get("/api/v1/manage/tracking")
                .thenReturn().path("events.size()");

        given().param("conversationId", conversationId)
                .param("perPage", numEvents)
                .param("page", 1)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(200)
                .and().body("events", iterableWithSize(numEvents));

        given().param("conversationId", conversationId)
                .param("perPage", numEvents)
                .param("page", 2)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(200)
                .and().body("events", emptyIterable());

        given().param("conversationId", conversationId)
                .param("perPage", 1)
                .param("page", 1)
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(200)
                .and().body("events", iterableWithSize(1));
    }
    
    private static void ensureAnswerDoesNotExist(String answerClass) {
        String path = "/api/v1/manage/answer/" + answerClass;
        if (get(path).statusCode() == 200) {
            delete(path);
        }
    }
}
