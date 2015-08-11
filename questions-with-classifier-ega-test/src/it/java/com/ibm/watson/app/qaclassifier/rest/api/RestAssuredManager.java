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

import org.junit.rules.ExternalResource;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.log.RequestLoggingFilter;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;

/**
 * This class ensures that RestAssured is only initialized once.
 * 
 * Use it in JUnits that will be using RestAssured like so:
 * <pre>
 * {@literal @}ClassRule
 * public static RestAssuredManager restManager = new RestAssuredManager();
 * </pre>
 */
public class RestAssuredManager extends ExternalResource {
    
    private static boolean init = false;

    public RestAssuredManager() {
    }
    
    @Override
    protected void before() throws Throwable {
        if(!init) {
            String defaultURI = "http://localhost:9080";
            String baseURI = System.getProperty("app.url");
            if (baseURI == null || baseURI.isEmpty()) {
                System.err.println("Missing required system property app.url.  Using default value " + defaultURI);
                baseURI = defaultURI;
            }
            RestAssured.baseURI = baseURI;
            RestAssured.filters(new ManageApiAuthFilter(), new RequestLoggingFilter(), new ResponseLoggingFilter());

            init = true;
        }
    }
}
