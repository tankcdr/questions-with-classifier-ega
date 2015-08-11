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

package com.ibm.watson.app.qaclassifier;

import static com.jayway.restassured.RestAssured.given;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.common.tools.utils.CliUtils;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer.TypeEnum;
import com.ibm.watson.app.qaclassifier.tools.FileUtils;
import com.ibm.watson.it.test.harness.model.ClassifierTestAssertData;
import com.jayway.restassured.http.ContentType;

public class AddQuestionsToMockClassifier {
    private static final int ALTERNATIVE_ANSWER_COUNT = 9;
    private static final double HIGH_CONFIDENCE = 0.95;
    private static final double MEDIUM_CONFIDENCE = 0.88;
    private static final double LOW_CONFIDENCE = 0.1;

    private static final String DEFAULT_URL = "http://localhost:9080";

    private static final String ASSERT_CLASSIFY_ENDPOINT = "/test/testharness/v1/assertClassify";
    private static final String ANSWER_STORE_ENDPOINT = "/api/v1/manage/answer";

    private static final String URL_OPTION = "l", URL_OPTION_LONG = "url";
    private static final String FILE_OPTION = "f", FILE_OPTION_LONG = "file";
    private static final String USER_OPTION = "u", USER_OPTION_LONG = "user";
    private static final String PASSWORD_OPTION = "p", PASSWORD_OPTION_LONG = "password";

    private String url;
    private String user;
    private String password;

    private List<TrainingQuestion> trainingQuestions;
    private List<String> alternateAnswerClasses;

    public AddQuestionsToMockClassifier(String url, String path, String user, String password) throws IOException {
        this.url = url;
        this.user = user;
        this.password = password;

        JsonElement parsed = new JsonParser().parse(FileUtils.loadFromFilesystemOrClasspath(path));
        Type trainingDataType = new TypeToken<List<TrainingQuestion>>() {}.getType();
        trainingQuestions = new Gson().fromJson(parsed.getAsJsonObject().get("training_data"), trainingDataType);

        // Select several answer classes to use for alternate answers
        Set<String> allAnswerClases = new HashSet<>();
        for (TrainingQuestion question : trainingQuestions) {
            allAnswerClases.addAll(question.classes);
        }
        List<String> sortedAnswerClasses = new ArrayList<>(allAnswerClases);
        Collections.sort(sortedAnswerClasses);
        alternateAnswerClasses = new ArrayList<>(ALTERNATIVE_ANSWER_COUNT);
        for (int i = 0; i < ALTERNATIVE_ANSWER_COUNT; i++) {
            alternateAnswerClasses.add(sortedAnswerClasses.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        Option urlOption = CliUtils.createOption(URL_OPTION, URL_OPTION_LONG, true,
                "The root URL of the application to connect to. If omitted, the default will be used (" + DEFAULT_URL + ")",
                false, URL_OPTION_LONG);
        Option pathOption = CliUtils.createOption(FILE_OPTION, FILE_OPTION_LONG, true,
                "The file to be used as training data, can point to the file system or the class path",
                true, FILE_OPTION_LONG);
        Option userOption = CliUtils.createOption(USER_OPTION, USER_OPTION_LONG, true,
                "The username for the manage API",
                true, USER_OPTION_LONG);
        Option passwordOption = CliUtils.createOption(PASSWORD_OPTION, PASSWORD_OPTION_LONG, true,
                "The password for the manage API",
                true, PASSWORD_OPTION_LONG);

        final Options options = CliUtils.buildOptions(urlOption, pathOption, userOption, passwordOption);

        CommandLine cmd;
        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Could not parse cmd line arguments.\n" + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(120, "java " + AddQuestionsToMockClassifier.class.getName(), null, options, null);
            return;
        }

        final String url = cmd.getOptionValue(URL_OPTION, DEFAULT_URL);
        final String path = cmd.getOptionValue(FILE_OPTION);
        final String user = cmd.getOptionValue(USER_OPTION);
        final String password = cmd.getOptionValue(PASSWORD_OPTION);

        AddQuestionsToMockClassifier mockSetup = new AddQuestionsToMockClassifier(url, path, user, password);

        System.out.println("Creating mock responses for the questions in " + path + "...");
        mockSetup.mockResponsesForTrainingData();

        System.out.println("Creating mock responses for the sample questions...");
        mockSetup.mockResponse(SampleQuestions.HIGH_CONFIDENCE, "mock_one_answer", HIGH_CONFIDENCE,
                "This is a question that will be answered by the mock classifier with high confidence");
        mockSetup.mockResponse(SampleQuestions.LOW_CONFIDENCE, "mock_multiple_answers", MEDIUM_CONFIDENCE,
                "This is a question that will be answered by the mock classifier with low confidence");
        mockSetup.mockResponse(SampleQuestions.NO_ANSWERS, "mock_no_answers", LOW_CONFIDENCE,
                "This is a question that will be answered by the mock classifier with very low confidence");
        System.out.println("Done.");
    }

    private void mockResponsesForTrainingData() {
        for (TrainingQuestion data : trainingQuestions) {
            mockResponse(data.text, data.classes.get(0), HIGH_CONFIDENCE, null);
        }
    }

    private void addAnswerClass(String answerClass, String answerText, String canonicalQuestion) {
        int statusCode = given().baseUri(url)
                .auth().basic(user, password)
                .contentType(ContentType.JSON)
                .when().get(ANSWER_STORE_ENDPOINT + "/" + answerClass)
                .thenReturn().statusCode();

        if (statusCode == 404) {
            given().baseUri(url)
                    .auth().basic(user, password)
                    .contentType(ContentType.JSON)
                    .body(Arrays.asList(new ManagedAnswer(answerClass, TypeEnum.TEXT, answerText, canonicalQuestion)))
                    .log().ifValidationFails()
                    .when().post(ANSWER_STORE_ENDPOINT)
                    .then().statusCode(200)
                    .and().log().ifValidationFails();
        }
    }

    private void mockResponse(String questionText, String answerClass, double confidence, String answerText) {

        if (answerText != null) {
            addAnswerClass(answerClass, answerText, questionText);
        }

        ClassifierTestAssertData payload = new ClassifierTestAssertData();
        payload.setText(questionText);

        NLClassiferClassifyResponse payloadResponse = new NLClassiferClassifyResponse();

        List<NLClassifiedClass> answerClasses = new ArrayList<>();
        answerClasses.add(new NLClassifiedClass(answerClass, confidence));
        answerClasses.addAll(createAlternateAnswers(confidence));

        payloadResponse.setClasses(answerClasses);
        payloadResponse.setClassifierId("mockClassifier");
        payloadResponse.setText(questionText);
        payloadResponse.setTopClass(answerClass);
        payloadResponse.setUrl("url");

        payload.setResponse(payloadResponse);

        given().baseUri(url)
                .contentType(ContentType.JSON)
                .body(payload)
                .log().ifValidationFails()
                .when().post(ASSERT_CLASSIFY_ENDPOINT)
                .then().statusCode(200)
                .and().log().ifValidationFails();

    }

    private List<NLClassifiedClass> createAlternateAnswers(double topAnswerConfidence) {
        double confidencePerAnswer = topAnswerConfidence / alternateAnswerClasses.size();

        List<NLClassifiedClass> classes = new ArrayList<>();
        for (String alternateAnswerClass : alternateAnswerClasses) {
            classes.add(new NLClassifiedClass(alternateAnswerClass, confidencePerAnswer));
        }

        return classes;
    }

    private class TrainingQuestion {
        String text;
        List<String> classes;
    }
}
