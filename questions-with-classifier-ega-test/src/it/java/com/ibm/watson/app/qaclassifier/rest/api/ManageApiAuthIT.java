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

import static com.jayway.restassured.RestAssured.given;

import org.junit.ClassRule;
import org.junit.Test;

public class ManageApiAuthIT {
    @ClassRule
    public static RestAssuredManager restManager = new RestAssuredManager();

    private static final String testAnswerClass = ManageApiIT.class.getSimpleName() + "_answer_class";

    @Test
    public void test_get_answers() {
        given().auth().none()
                .when().get("/api/v1/manage/answer")
                .then().statusCode(401);
    }

    @Test
    public void test_post_answers() {
        given().auth().none()
                .when().post("/api/v1/manage/answer")
                .then().statusCode(401);
    }

    @Test
    public void test_delete_answer() {
        given().auth().none()
                .when().delete("/api/v1/manage/answer/" + testAnswerClass)
                .then().statusCode(401);
    }

    @Test
    public void test_get_answer() {
        given().auth().none()
                .when().get("/api/v1/manage/answer/" + testAnswerClass)
                .then().statusCode(401);
    }

    @Test
    public void test_put_answer() {
        given().auth().none()
                .when().put("/api/v1/manage/answer/" + testAnswerClass)
                .then().statusCode(401);
    }

    @Test
    public void test_get_tracking_events() {
        given().auth().none()
                .when().get("/api/v1/manage/tracking")
                .then().statusCode(401);
    }
}
