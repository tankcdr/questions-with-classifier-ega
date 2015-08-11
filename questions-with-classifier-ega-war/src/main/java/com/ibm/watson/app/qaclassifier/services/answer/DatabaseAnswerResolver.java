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

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.services.entities.AnswerEntity;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class DatabaseAnswerResolver implements AnswerResolver {
    
    private final PersistenceEntityProvider provider;
    private final CanonicalQuestionService canonicalQuestionService;

    @Inject
    public DatabaseAnswerResolver(PersistenceEntityProvider provider, CanonicalQuestionService canonicalQuestionService) {
        this.provider = provider; 
        this.canonicalQuestionService = canonicalQuestionService;
    }

    @Override
    public Answer resolve(NLClassifiedClass classifiedClass) throws ResolutionException {
        final String className = classifiedClass.getClassName();
        final Double confidence = classifiedClass.getConfidence();
        final ConfidenceCategoryEnum confidenceCategory = null; // This is now done on the entire answer list, after answer resolution
        
        AnswerEntity answerEntity = getRowFromDB(className);
        
        return new Answer(answerEntity.getAnswerType(), answerEntity.getAnswerText(), confidence,
                confidenceCategory, answerEntity.getCanonicalQuestion(), answerEntity.getAnswerClass());
    }
    
    private AnswerEntity getRowFromDB(String className) throws ResolutionException {
        EntityManager em = provider.getEntityManager();
        AnswerEntity answerEntity;
        try {
            answerEntity = em.find(AnswerEntity.class, className);
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
        if(answerEntity == null) {
        	throw new ResolutionException(MessageKey.AQWQAC24150E_could_not_find_key_in_db_1.getMessage(className).getFormattedMessage());
        }
        
        // Currently the DB might not have our all the canonical question data, so use the service as a fallback
        if(answerEntity.getCanonicalQuestion() == null) {
            answerEntity.setCanonicalQuestion(canonicalQuestionService.lookup(className));
        }
        
        return answerEntity;
    }
}
