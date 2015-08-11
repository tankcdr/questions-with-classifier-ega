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

import com.jayway.restassured.authentication.NoAuthScheme;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;

/**
 * A REST Assured Filter that will add username and password information
 * to any request that uses the /manage API.
 * 
 * Usage example:
 * given().filter(new ManageApiAuthFilter()).get("/api/v1/manage/answer");
 */
public class ManageApiAuthFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        String password = System.getProperty("manage.api.password");

        // Only set the auth scheme if it hasn't already been set to something else.
        if (requestSpec.getAuthenticationScheme() instanceof NoAuthScheme
                && ctx.getCompleteRequestPath().contains("/manage/")
                && password != null && !password.isEmpty()) {
            requestSpec.auth().basic("apiuser", password);
        }

        return ctx.next(requestSpec, responseSpec);
    }

}
