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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.watson.app.qaclassifier.rest.model.ErrorResponse;

public abstract class AbstractRestApiImpl {
    
    protected Response getOkResponse(Object entity) {
        return Response.ok(entity).build();
    }
    
    protected Response getBadRequestResponse(String errorMessage) {
        return Response.status(Status.BAD_REQUEST).entity(new ErrorResponse(Status.BAD_REQUEST.getStatusCode(), errorMessage)).build();
    }
    
    protected Response getErrorResponse(String errorMessage) {
        return Response.serverError().entity(new ErrorResponse(Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorMessage)).build();
    }
    
    protected Response getNotFoundResponse() {
        return Response.status(Status.NOT_FOUND).build();
    }
}
