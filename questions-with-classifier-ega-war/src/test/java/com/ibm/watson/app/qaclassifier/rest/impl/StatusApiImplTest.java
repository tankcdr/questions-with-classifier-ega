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

package com.ibm.watson.app.qaclassifier.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.rest.impl.StatusApiImpl;

@RunWith(PowerMockRunner.class)
public class StatusApiImplTest extends BaseRestApiTest {
    
    private StatusApiImpl statusApi;
    private Response response;
    
    @Test
    public void test_status_api() throws Exception {
        GIVEN.the_status_api_impl_is_created();
            AND.response_builder_class_is_mocked();
        WHEN.get_status_is_invoked();
        THEN.the_response_is_not_null();
            AND.the_return_code_is(Status.OK.getStatusCode());
            AND.the_message_is("good");
    }
    
    private void the_message_is(String expectedString) {
        Object entity = response.getEntity();
        assertTrue(entity instanceof String);
        
        final JsonParser parser = new JsonParser();
        JsonElement json = null;
        try {
            json = parser.parse((String) entity);
        } catch(JsonSyntaxException e) {
            fail("Invalid JSON received from response entity: " + e.getMessage());
        }
        
        assertNotNull("The response payload is not valid JSON", json);
        assertTrue(json.isJsonObject()); 
        
        JsonObject jsonObject = json.getAsJsonObject();
        assertTrue(jsonObject.has("status"));
        assertTrue(jsonObject.get("status").getAsString().equals(expectedString));
    }

    private void the_return_code_is(int i) {
        assertEquals(i, response.getStatus());
    }
    
    private void the_response_is_not_null() {
        assertNotNull(response);
    }

    private void get_status_is_invoked() throws NotFoundException {
        response = statusApi.getStatus();
    }

    private void the_status_api_impl_is_created() throws NotFoundException {
        statusApi = new StatusApiImpl();
    }

    @SuppressWarnings("unused")
    private final StatusApiImplTest GIVEN = this, WHEN = this, THEN = this, WITH = this, AND = this;
}
