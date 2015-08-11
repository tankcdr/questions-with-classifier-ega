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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({ResponseBuilder.class})
@PowerMockIgnore("javax.management.*")
public class BaseRestApiTest {
    @Mock
    private ResponseBuilder responseBuilder;    
    @Mock
    private Response mockResponse;
    
    private Object entity;
    private StatusType status;

    protected void response_builder_class_is_mocked() throws Exception {
        PowerMockito.mockStatic(ResponseBuilder.class);        
        PowerMockito.when(ResponseBuilder.class, "newInstance").thenReturn(responseBuilder);
        when(responseBuilder.entity(any(Object.class))).thenAnswer(new Answer<ResponseBuilder>() {
            @Override
            public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
                entity = invocation.getArguments()[0];
                return responseBuilder;
            }
        });
        when(responseBuilder.status(any(StatusType.class))).thenAnswer(new Answer<ResponseBuilder>() {
            @Override
            public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
                status = (StatusType) invocation.getArguments()[0];
                return responseBuilder;
            }            
        });
        when(responseBuilder.build()).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return entity;
            }
        });
        when(mockResponse.getStatus()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return status.getStatusCode();
            }
        });
    }
}
