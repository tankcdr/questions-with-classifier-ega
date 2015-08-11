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
package com.ibm.watson.app.qaclassifier.services.rest.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.services.rest.ResponseCache;

public class InMemoryResponseCache implements ResponseCache {
    
    private final Cache<String, List<Answer>> responseCache = CacheBuilder.newBuilder()
            .initialCapacity(50)
            .concurrencyLevel(5)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    @Override
    public List<Answer> get(String conversationId) {
        List<Answer> retval = responseCache.getIfPresent(conversationId);
        if(retval == null) {
            retval = Collections.emptyList();
        }
        return retval;
    }
    
    @Override
    public void put(String conversationId, List<Answer> answers) {
        responseCache.put(conversationId, answers);
    }
}
