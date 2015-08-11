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
package com.ibm.watson.app.qaclassifier.services.rest;

import java.util.List;

import com.ibm.watson.app.qaclassifier.rest.model.Answer;

public interface ResponseCache {
    /**
     * Returns the cached list of answers for the given conversation ID
     * Note: Implementations are required to never return {@code null} from this method. 
     * @param conversationId
     * @return A list of cached answers
     */
    public List<Answer> get(String conversationId);
    public void put(String conversationId, List<Answer> answers);
}
