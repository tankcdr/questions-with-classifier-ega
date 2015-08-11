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

package com.ibm.watson.app.qaclassifier.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.common.services.box.BoxService;
import com.ibm.watson.app.common.services.box.impl.BoxServiceImpl;
import com.ibm.watson.app.common.services.general.ConfigurationService;
import com.ibm.watson.app.common.services.general.impl.GeneralConfigurationService;
import com.ibm.watson.app.common.services.impl.BluemixServicesBinder;
import com.ibm.watson.app.common.services.nlclassifier.NLClassifierService;
import com.ibm.watson.app.qaclassifier.services.answer.AnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.BoxAnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.CanonicalQuestionService;
import com.ibm.watson.app.qaclassifier.services.answer.DatabaseAnswerResolver;
import com.ibm.watson.app.qaclassifier.services.answer.TrainingJsonCanonicalQuestionService;
import com.ibm.watson.app.qaclassifier.services.rest.ConfidenceCategorizer;
import com.ibm.watson.app.qaclassifier.services.rest.ConversationManager;
import com.ibm.watson.app.qaclassifier.services.rest.ResponseCache;
import com.ibm.watson.app.qaclassifier.services.rest.TopQuestionsStore;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger;
import com.ibm.watson.app.qaclassifier.services.rest.impl.CumulativeConfidenceCategorizer;
import com.ibm.watson.app.qaclassifier.services.rest.impl.DatabaseConversationManager;
import com.ibm.watson.app.qaclassifier.services.rest.impl.InMemoryResponseCache;
import com.ibm.watson.app.qaclassifier.services.rest.impl.TopQuestionsJsonStore;
import com.ibm.watson.app.qaclassifier.services.rest.impl.UserTrackingDBLogger;
import com.ibm.watson.app.qaclassifier.services.rest.impl.UserTrackingImpl;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class ClassifierServicesModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger();
    private static final String CONFIG_NAME = "/config/config.properties";

    private static final String ANSWERSTORE_PROPERTY = "answer.store";
    private enum ANSWERSTORE_TYPE { BOX, DATABASE };
    
    @Override
    protected void configure() {
        bind(ConfidenceCategorizer.class).to(CumulativeConfidenceCategorizer.class).in(Singleton.class);
        bind(UserTracking.class).to(UserTrackingImpl.class).in(Singleton.class);
        bind(UserTrackingLogger.class).to(UserTrackingDBLogger.class).in(Singleton.class);
        bind(CanonicalQuestionService.class).to(TrainingJsonCanonicalQuestionService.class).in(Singleton.class);
        bind(TopQuestionsStore.class).to(TopQuestionsJsonStore.class).in(Singleton.class);
        bind(ConversationManager.class).to(DatabaseConversationManager.class).in(Singleton.class);
        bind(ResponseCache.class).to(InMemoryResponseCache.class).in(Singleton.class);
        bind(BoxService.class).to(BoxServiceImpl.class).in(Singleton.class);
        
        requireBinding(PersistenceEntityProvider.class); // DatabaseAnswerResolver requires the database
        
        // Bluemix services
        BluemixServicesBinder.bindAll(binder(), NLClassifierService.class);
    }

    // add override to give access to test cases.
    @Override
    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return super.bind(clazz);
    }
    
    @Provides
    protected ConfigurationService getConfigService() {
        try(InputStream stream = getClass().getResourceAsStream(CONFIG_NAME)) {
            return new GeneralConfigurationService(stream);
        } catch (IOException e) {
        	logger.error(MessageKey.AQWQAC24000E_unable_load_conf_from_stream_using_empty_conf_service.getMessage());
            return new GeneralConfigurationService(new Properties());
        }
    }
    
    @Provides
    protected AnswerResolver getAnswerResolver(ConfigurationService configService, PersistenceEntityProvider provider, CanonicalQuestionService canonicalQuestionService, BoxService boxService) {
    	ANSWERSTORE_TYPE storeProp = ANSWERSTORE_TYPE.valueOf(configService.getProperty(ANSWERSTORE_PROPERTY, "DATABASE").toUpperCase());
    	if( storeProp == ANSWERSTORE_TYPE.BOX ) {
    		return new BoxAnswerResolver(boxService);
    	}
    	else {
    		return new DatabaseAnswerResolver(provider, canonicalQuestionService);
    	}
    }
}
