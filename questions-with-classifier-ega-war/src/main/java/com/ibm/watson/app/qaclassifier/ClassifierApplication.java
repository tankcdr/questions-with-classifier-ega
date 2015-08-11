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

package com.ibm.watson.app.qaclassifier;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.ibm.watson.app.qaclassifier.rest.ConversationApi;
import com.ibm.watson.app.qaclassifier.rest.FeedbackApi;
import com.ibm.watson.app.qaclassifier.rest.ManageApi;
import com.ibm.watson.app.qaclassifier.rest.StatusApi;
import com.wordnik.swagger.jaxrs.json.JacksonJsonProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResource;

public class ClassifierApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(ApiListingResource.class);
		classes.add(StatusApi.class);
        classes.add(ConversationApi.class);
        classes.add(FeedbackApi.class);
        classes.add(ManageApi.class);
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new JacksonJsonProvider());
		return singletons;
	}
}
