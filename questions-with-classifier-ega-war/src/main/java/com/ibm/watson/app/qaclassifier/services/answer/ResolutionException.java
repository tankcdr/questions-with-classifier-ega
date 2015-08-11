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

package com.ibm.watson.app.qaclassifier.services.answer;

/**
 * This exception indicates that an intent returned from the classifier service
 * cannot be resolved to a proper answer. 
 */
public class ResolutionException extends Exception {
    private static final long serialVersionUID = -307736035565403192L;

    public ResolutionException(String message) {
        super(message);
    }

    public ResolutionException(Throwable cause) {
        super(cause);
    }

    public ResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
