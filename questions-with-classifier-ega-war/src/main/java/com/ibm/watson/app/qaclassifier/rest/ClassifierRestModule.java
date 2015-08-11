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

package com.ibm.watson.app.qaclassifier.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.ibm.watson.app.qaclassifier.rest.impl.ConversationApiImpl;
import com.ibm.watson.app.qaclassifier.rest.impl.FeedbackApiImpl;
import com.ibm.watson.app.qaclassifier.rest.impl.ManageApiImpl;
import com.ibm.watson.app.qaclassifier.rest.impl.StatusApiImpl;

public class ClassifierRestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StatusApiInterface.class).to(StatusApiImpl.class).in(Singleton.class);
        bind(ConversationApiInterface.class).to(ConversationApiImpl.class).in(Singleton.class);
        bind(FeedbackApiInterface.class).to(FeedbackApiImpl.class).in(Singleton.class);
        bind(ManageApiInterface.class).to(ManageApiImpl.class).in(Singleton.class);
    }

    // add override to give access to test cases.
    @Override
    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return super.bind(clazz);
    }

}
