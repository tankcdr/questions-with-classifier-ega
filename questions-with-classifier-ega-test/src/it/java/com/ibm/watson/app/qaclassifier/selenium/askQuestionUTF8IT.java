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

package com.ibm.watson.app.qaclassifier.selenium;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import net.jcip.annotations.NotThreadSafe;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.google.gson.Gson;
import com.ibm.watson.app.qaclassifier.SampleQuestions;
import com.ibm.watson.app.qaclassifier.rest.api.RestAssuredManager;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer.TypeEnum;
import com.ibm.watson.app.qaclassifier.rest.model.MessageRequest;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;

@NotThreadSafe // Due to test cases modifying the answer store
@RunWith(Multiplatform.class)
public class askQuestionUTF8IT {
	@InjectDriver
	public WebDriver driver;

	@ClassRule
	public static RestAssuredManager restManager = new RestAssuredManager();

	private static final String api_conversation = "/api/v1/conversation";
	private static final String api_ask_question = api_conversation
			+ "/{conversationId}";

	@Test
	public void askQuestionUTF8() {
		String questionText = "Что такое NL Classifier 我叫王睿健?";

		CommonFunctions.askQuestionViaTextInput(driver, questionText);

		assertThat(
				"After asking question via text input using UTF-8 chars, didn't find same question text on answer page",
				getDisplayedQuestionText(), containsString(questionText));
	}

	@Test
	public void verifyAnswerInUTF8() throws UnsupportedEncodingException {
		String answerText;
		String newAnswerText = "今天天气不错";

		// oldAnswer[0] is the classname, oldAnswer[1] is the answer text
		String[] oldAnswer = getAnswerClassFromPredictableQuestion();

		try {
			generateAnswerJsonAndPut(oldAnswer[0], TypeEnum.TEXT, newAnswerText, SampleQuestions.HIGH_CONFIDENCE);

			CommonFunctions.askQuestionViaTextInput(driver, SampleQuestions.HIGH_CONFIDENCE);
			answerText = getDisplayedAnswerText();
		} 
		finally {
			// Restore our local db
			generateAnswerJsonAndPut(oldAnswer[0], TypeEnum.TEXT, oldAnswer[1], SampleQuestions.HIGH_CONFIDENCE);
		}

		assertThat(
				"After modifying Answer store to provide a UTF-8 double byte answer, it is properly shown",
				answerText, containsString(newAnswerText));
	}

	private void generateAnswerJsonAndPut(String className, TypeEnum type,
			String answerText, String canonoicalQuestion)
			throws UnsupportedEncodingException {

		// Access and modify the Answer store to place our answer text in it
		// Maybe a better way to convert this to JSON
		ManagedAnswer answer = new ManagedAnswer();
		answer.setClassName(className);
		answer.setType(type);
		answer.setText(answerText);
		answer.setCanonicalQuestion(canonoicalQuestion);
		String json = new Gson().toJson(answer);
		byte[] utf8Json = json.getBytes(StandardCharsets.UTF_8);

		given().contentType(ContentType.JSON).request().body(utf8Json)
				.put("/api/v1/manage/answer/" + className).then()
				.statusCode(200);
	}

	private String[] getAnswerClassFromPredictableQuestion() {

		JsonPath p = given()
				.pathParam("conversationId", getAConversationId())
				.contentType(ContentType.JSON)
				.body(new MessageRequest(SampleQuestions.HIGH_CONFIDENCE, null))
				.when().post(api_ask_question)
				.then().statusCode(200)
				.and().extract().jsonPath();

		String[] responses = new String[2];

		responses[0] = p.getString("responses[0].className");
		responses[1] = p.getString("responses[0].text");

		return responses;
	}

	private String getAConversationId() {
		return post(api_conversation).then().extract().path("conversationId");
	}

	private String getDisplayedQuestionText() {
		return driver.findElement(By.className("question-text")).getText();
	}

	private String getDisplayedAnswerText() {
		return driver.findElement(By.className("answer-quote")).getText();
	}
}
