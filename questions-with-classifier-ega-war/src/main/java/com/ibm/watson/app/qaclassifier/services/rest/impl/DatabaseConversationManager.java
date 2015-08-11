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

package com.ibm.watson.app.qaclassifier.services.rest.impl;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.ibm.watson.app.common.persistence.jpa.ApplicationTransaction;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ConversationEntity;
import com.ibm.watson.app.qaclassifier.services.rest.ConversationManager;

public class DatabaseConversationManager implements ConversationManager {
    
    private final PersistenceEntityProvider provider;
    
    @Inject
    public DatabaseConversationManager(PersistenceEntityProvider provider) {
        this.provider = provider;
    }

    @Override
    public String getNewConversationId() {
        EntityManager em = provider.getEntityManager();
        ApplicationTransaction et = provider.getTransaction(em);

        ConversationEntity c = new ConversationEntity();

        try {
            et.begin();
            em.persist(c);
            et.commit();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }

        return String.valueOf(c.getId());
    }

}
