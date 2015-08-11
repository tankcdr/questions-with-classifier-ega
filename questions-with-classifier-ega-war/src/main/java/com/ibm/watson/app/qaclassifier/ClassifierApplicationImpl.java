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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.watson.app.common.persistence.jpa.impl.ApplicationPersistenceModule;
import com.ibm.watson.app.qaclassifier.rest.ClassifierRestModule;
import com.ibm.watson.app.qaclassifier.rest.ManageApiInterface;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.services.ClassifierServicesModule;

//@ApplicationPath("/api/v1")
public class ClassifierApplicationImpl extends ClassifierApplication {
    
    // This must match the value in persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "com.ibm.watson.app.qaclassifier.db";
    private final Injector injector;

    public ClassifierApplicationImpl() {
        injector = Guice.createInjector(new ClassifierRestModule(), new ClassifierServicesModule(), new ApplicationPersistenceModule(PERSISTENCE_UNIT_NAME));
        
        // defect 97447 - used as a workaround for JPA deadlocks - the very first call to the database in a "cloud" environment should be a
        // fetch so that its initialized, follow-on release will handle this more elegantly
        ManageApiInterface manage = injector.getInstance(ManageApiInterface.class);
        try {
            manage.getAnswers();
        } 
        catch (NotFoundException e) {
            // do nothing, we expect to find nothing
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        final Set<Object> singletons = super.getSingletons();
        for (Class<?> c : super.getClasses()) {
            singletons.add(injector.getInstance(c));
        }
        return singletons;
    }
}
