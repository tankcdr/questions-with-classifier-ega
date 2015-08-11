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
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.ibm.watson.app.qaclassifier.SampleQuestions;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequest;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequestReferrer.SourceEnum;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.ResponseSpecification;

public class ConversationApiIT {
    @ClassRule
    public static TestRule restAssuredManager = new RestAssuredManager();

    private static final String api_conversation = "/api/v1/conversation";
    private static final String api_ask_question = api_conversation + "/{conversationId}";
    private static final String api_top_questions = api_conversation + "/{conversationId}/topQuestions";

    ResponseSpecification topQuestionsSpec = new ResponseSpecBuilder()
            .expectStatusCode(HttpStatus.SC_OK)
            .expectBody("topQuestions", not(empty()))
            .expectBody("topQuestions.questionText", everyItem(instanceOf(String.class)))
            .build();

    ResponseSpecification conversationSpec = new ResponseSpecBuilder()
            .expectStatusCode(HttpStatus.SC_OK)
            .expectBody("conversationId", not(isEmptyOrNullString()))
            .addResponseSpecification(topQuestionsSpec)
            .build();

    ResponseSpecification askQuestionSpec = new ResponseSpecBuilder()
            .expectStatusCode(HttpStatus.SC_OK)
            .expectBody("messageId", not(isEmptyOrNullString()))
            .expectBody("message", not(isEmptyOrNullString()))
            .expectBody("responses", everyItem(instanceOf(Map.class)))
            .build();

    @Test
    public void startConversation() {
        when().post(api_conversation)
                .then().spec(conversationSpec);
    }

    @Test
    public void startMultipleConversations() {
        String conversationId = when().post(api_conversation)
                .then().spec(conversationSpec)
                .and().extract().path("conversationId");

        when().post(api_conversation)
                .then().spec(conversationSpec)
                .and().body("conversationId", not(equalTo(conversationId)));
    }

    @Test
    public void getTopQuestions() {
        given().pathParam("conversationId", getAConversationId())
                .when().get(api_top_questions)
                .then().spec(topQuestionsSpec);
    }

    @Ignore("Conversation ID validation not implemented")
    @Test
    public void getTopQuestionsWithInvalidConversationId() {
        given().pathParam("conversationId", "MyInvalidConversationId")
                .when().get(api_top_questions)
                .then().statusCode(400);
    }

    @Test
    public void askQuestion() {
        given().pathParam("conversationId", getAConversationId())
                .contentType(ContentType.JSON)
                .body(new MessageRequest(SampleQuestions.HIGH_CONFIDENCE, null))
                .when().post(api_ask_question)
                .then().spec(askQuestionSpec);
    }

    @Test
    public void askQuestionWithoutContentType() {
        given().pathParam("conversationId", getAConversationId())
                .body(new MessageRequest(SampleQuestions.HIGH_CONFIDENCE, null))
                .when().post(api_ask_question)
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void askQuestionWithReferrer() {
        String conversationId = getAConversationId();

        JsonPath jsonPath = given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(SampleQuestions.HIGH_CONFIDENCE, null))
                .post(api_ask_question)
                .then().extract().jsonPath();

        String messageId = jsonPath.getString("messageId");
        String refinementQuestion = jsonPath.getString("responses[1].canonicalQuestion");

        given().pathParam("conversationId", conversationId)
                .contentType(ContentType.JSON)
                .body(new MessageRequest(refinementQuestion, new MessageRequestReferrer(messageId, SourceEnum.REFINEMENT)))
                .when().post(api_ask_question)
                .then().spec(askQuestionSpec);
    }

    private String getAConversationId() {
        return post(api_conversation).then().extract().path("conversationId");
    }
}
