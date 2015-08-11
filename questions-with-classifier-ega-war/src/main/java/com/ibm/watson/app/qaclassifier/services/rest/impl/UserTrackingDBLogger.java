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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.watson.app.common.persistence.jpa.ApplicationTransaction;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ClickEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ImpressionEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.QueryEntity;
import com.ibm.watson.app.qaclassifier.services.jpa.EntityConstants;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger;

public class UserTrackingDBLogger implements UserTrackingLogger {

    private final PersistenceEntityProvider provider;
    private static final Logger logger = LogManager.getLogger();

    @Inject
    public UserTrackingDBLogger(PersistenceEntityProvider provider) {
        this.provider = provider;
    }

    @Override
    public long getPositiveImpressionIdForMessage(long messageId) {
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getPrompt().equals(UserTracking.THIS_WAS_HELPFUL)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public long getNegativeImpressionIdForMessage(long messageId) {
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getPrompt().equals(UserTracking.I_STILL_NEED_HELP)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public long getImpressionIdForMessage(long messageId, String questionText) {
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getPrompt().equals(questionText)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public long getNoneOfTheAboveImpressionIdForMessage(long messageId) {
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getPrompt().equals(UserTracking.NONE_OF_THE_ABOVE)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public long getForumVisitImpressionIdForMessage(long messageId) {
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getPrompt().equals(UserTracking.VISIT_THE_FORUM)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public List<Long> getHiddenImpressionIdsForMessage(long messageId) {
        List<Long> impressionIds = new ArrayList<>();
        for (ImpressionEntity im : getImpressions(messageId)) {
            if (im.getLocation().equals(Location.HIDDEN)) {
                impressionIds.add(im.getId());
            }
        }
        return impressionIds;
    }

    @Override
    public long getTopQuestionImpressionId(long conversationId, String questionText) {
        for (ImpressionEntity im : getTopQuestionImpressions(conversationId)) {
            if (im.getPrompt().equals(questionText)) {
                return im.getId();
            }
        }
        return 0;
    }

    @Override
    public long query(Date timestamp, String user, String hostname, Long referral, String questionText, InputMode mode) {
        EntityManager em = provider.getEntityManager();

        ClickEntity referringClick = referral == null ? null : em.find(ClickEntity.class, referral);
        QueryEntity q = new QueryEntity(timestamp, user, hostname, referringClick, questionText, mode);

        persistAndClose(q, em);
        logger.debug(q);
        return q.getId();
    }

    @Override
    public long impression(Date timestamp, String user, String hostname, Long referral, String prompt,
            String classifiedClass, int offset, String action, Location location, Provenance provenance, double confidence) {
        EntityManager em = provider.getEntityManager();

        QueryEntity referringQuery = referral == null ? null : em.find(QueryEntity.class, referral);
        ImpressionEntity im = new ImpressionEntity(timestamp, user, hostname, referringQuery, prompt,
                classifiedClass, offset, action, location, provenance, confidence);

        persistAndClose(im, em);
        if (!location.equals(Location.HIDDEN)) {
            logger.trace(im);
        }
        return im.getId();
    }

    @Override
    public long click(Date timestamp, String user, String hostname, Long referral) {
        EntityManager em = provider.getEntityManager();

        ClickEntity c = new ClickEntity(timestamp, user, hostname, em.find(ImpressionEntity.class, referral));

        persistAndClose(c, em);
        logger.trace(c);
        return c.getId();
    }

    @Override
    public void unhideImpression(long impressionId, Location newLocation) {
        EntityManager em = provider.getEntityManager();

        ImpressionEntity im = em.find(ImpressionEntity.class, impressionId);
        im.setTimestamp(new Date());
        im.setLocation(newLocation);

        mergeAndClose(im, em);
        logger.trace(im);
    }

    private Set<ImpressionEntity> getImpressions(long messageId) {
        EntityManager em = provider.getEntityManager();
        try {
            return em.find(QueryEntity.class, messageId).getImpressions();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    /**
     * Returns the list of top question impressions for this conversation,
     * sorted from newest to oldest.
     * 
     * @param conversationId
     * @return
     */
    private List<ImpressionEntity> getTopQuestionImpressions(long conversationId) {
        EntityManager em = provider.getEntityManager();
        try {
            TypedQuery<ImpressionEntity> query = em.createNamedQuery(EntityConstants.QUERY_TOP_QUESTION_IMPRESSIONS, ImpressionEntity.class);
            query.setParameter("user", String.valueOf(conversationId));
            return query.getResultList();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private void persistAndClose(Object o, EntityManager em) {
        ApplicationTransaction et = provider.getTransaction(em);
        try {
            et.begin();
            em.persist(o);
            et.commit();
        } catch (Exception e) {
            if (et.isActive()) {
                et.rollback();
            }
            throw e;
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private void mergeAndClose(Object o, EntityManager em) {
        ApplicationTransaction et = provider.getTransaction(em);
        try {
            et.begin();
            em.merge(o);
            et.commit();
        } catch (Exception e) {
            if (et.isActive()) {
                et.rollback();
            }
            throw e;
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }
}
