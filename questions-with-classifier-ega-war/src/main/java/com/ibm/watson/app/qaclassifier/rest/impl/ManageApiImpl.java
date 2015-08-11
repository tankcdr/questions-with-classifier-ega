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

package com.ibm.watson.app.qaclassifier.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.ibm.watson.app.common.persistence.jpa.ApplicationTransaction;
import com.ibm.watson.app.common.persistence.jpa.PersistenceEntityProvider;
import com.ibm.watson.app.qaclassifier.rest.ManageApiInterface;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.ManagedAnswer;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingEvent;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingEvent.TypeEnum;
import com.ibm.watson.app.qaclassifier.rest.model.TrackingResponse;
import com.ibm.watson.app.qaclassifier.services.entities.AnswerEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ClickEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.ImpressionEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.QueryEntity;
import com.ibm.watson.app.qaclassifier.services.entities.tracking.TrackingEventEntity;
import com.ibm.watson.app.qaclassifier.services.jpa.EntityConstants;

public class ManageApiImpl extends AbstractRestApiImpl implements ManageApiInterface {

    private final PersistenceEntityProvider provider;

    @Inject
    public ManageApiImpl(PersistenceEntityProvider provider) {
        this.provider = provider;
    }

    @Override
    public Response getAnswers() throws NotFoundException {
        final List<ManagedAnswer> answers = new ArrayList<>();

        List<AnswerEntity> entities = getAllAnswersFromDatabase();
        for (AnswerEntity entity : entities) {
            answers.add(mapEntityToManagedAnswer(entity));
        }

        return Response.ok(answers).build();
    }

    @Override
    public Response createAnswer(List<ManagedAnswer> answers) throws NotFoundException {
        List<AnswerEntity> entities = new ArrayList<>();
        for (ManagedAnswer answer : answers) {
            entities.add(mapManagedAnswerToEntity(answer));
        }
        try {
            saveAnswers(entities);
        } catch (Exception e) {
            return getErrorResponse(e.getMessage());
        }
        return Response.ok().build();
    }

    @Override
    public Response getAnswer(String className) throws NotFoundException {
        AnswerEntity entity = getAnswerFromDatabase(className);
        if (entity == null) {
            return getNotFoundResponse();
        }
        return Response.ok(mapEntityToManagedAnswer(entity)).build();
    }

    @Override
    public Response updateAnswer(String className, ManagedAnswer answer) throws NotFoundException {
        if (!answer.getClassName().equals(className)) {
            return getBadRequestResponse("Cannot change class name");
        }

        AnswerEntity entity = getAnswerFromDatabase(className);
        if (entity == null) {
            return getNotFoundResponse();
        }

        try {
            updateAnswer(mapManagedAnswerToEntity(answer));
        } catch (Exception e) {
            return getErrorResponse(e.getMessage());
        }

        return Response.ok().build();
    }

    @Override
    public Response deleteAnswer(String className) throws NotFoundException {
        AnswerEntity entity = getAnswerFromDatabase(className);
        if (entity == null) {
            return getNotFoundResponse();
        }

        try {
            deleteAnswerFromDatabase(entity);
        } catch (Exception e) {
            return getErrorResponse(e.getMessage());
        }

        return Response.ok().build();
    }

    @Override
    public Response getTrackingEvents(String conversationId, Integer page, Integer perPage) throws NotFoundException {
        int maxPageSize = 50000;
        if (perPage == null) {
            perPage = maxPageSize;
        }
        if (page == null) {
            page = 1;
        }

        if (perPage > maxPageSize) {
            return getBadRequestResponse("Maximum page size is " + maxPageSize);
        }
        if (perPage < 1) {
            return getBadRequestResponse("Minimum page size is 1");
        }
        if (page < 1) {
            return getBadRequestResponse("Page index must be greater than 0");
        }

        List<TrackingEvent> events = new ArrayList<>();
        EntityManager em = provider.getEntityManager();
        try {
            TypedQuery<TrackingEventEntity> query;
            if (conversationId == null) {
                query = em.createNamedQuery(EntityConstants.QUERY_ALL_EVENTS, TrackingEventEntity.class);
            } else {
                query = em.createNamedQuery(EntityConstants.QUERY_CONVERSATION_EVENTS, TrackingEventEntity.class);
                query.setParameter("user", conversationId);
            }

            query.setFirstResult((page - 1) * perPage);
            query.setMaxResults(perPage);

            for (TrackingEventEntity event : query.getResultList()) {
                events.add(mapTrackingEventEntityToTrackingEvent(event));
            }
        } catch (Exception e) {
            return getErrorResponse(e.getMessage());
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
        if (conversationId != null && page == 1 && events.isEmpty()) {
            return getBadRequestResponse("No tracking events found for conversationId=" + conversationId);
        }

        return Response.ok(new TrackingResponse(events)).build();
    }

    private TrackingEvent mapTrackingEventEntityToTrackingEvent(TrackingEventEntity event) {
        TypeEnum eventType = null;
        Map<String, String> eventArgs = new HashMap<>();
        if (event instanceof ClickEntity) {
            eventType = TypeEnum.CLICK;
        } else if (event instanceof QueryEntity) {
            eventType = TypeEnum.QUERY;
            QueryEntity q = (QueryEntity) event;
            eventArgs.put("string", q.getString());
            eventArgs.put("mode", q.getMode().toString());
        } else if (event instanceof ImpressionEntity) {
            eventType = TypeEnum.IMPRESSION;
            ImpressionEntity im = (ImpressionEntity) event;
            eventArgs = new HashMap<>();
            eventArgs.put("prompt", im.getPrompt());
            eventArgs.put("class", im.getClassifiedClass());
            eventArgs.put("offset", String.valueOf(im.getOffset()));
            eventArgs.put("action", im.getAction());
            eventArgs.put("location", im.getLocation().toString());
            eventArgs.put("provenance", im.getProvenance().toString());
            eventArgs.put("confidence", im.getConfidence().toString());
        }

        String referralId = event.getReferral() == null ? null : String.valueOf(event.getReferral().getId());

        return new TrackingEvent(eventType, String.valueOf(event.getId()), event.getTimestamp(),
                event.getUser(), event.getHost(), referralId, eventArgs);
    }

    private AnswerEntity getAnswerFromDatabase(String className) {
        EntityManager em = provider.getEntityManager();
        try {
            return em.find(AnswerEntity.class, className);
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private List<AnswerEntity> getAllAnswersFromDatabase() {
        EntityManager em = provider.getEntityManager();
        try {
            TypedQuery<AnswerEntity> query = em.createNamedQuery(EntityConstants.QUERY_ALL_ANSWERS, AnswerEntity.class);
            return query.getResultList();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private void saveAnswers(List<AnswerEntity> entities) {
        EntityManager em = provider.getEntityManager();
        ApplicationTransaction trans = provider.getTransaction(em);
        try {
            trans.begin();
            for (AnswerEntity entity : entities) {
                em.persist(entity);
            }
            trans.commit();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private void updateAnswer(AnswerEntity entity) {
        EntityManager em = provider.getEntityManager();
        ApplicationTransaction trans = provider.getTransaction(em);
        try {
            trans.begin();
            em.merge(entity);
            trans.commit();
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private void deleteAnswerFromDatabase(AnswerEntity entity) {
        EntityManager em = provider.getEntityManager();
        ApplicationTransaction trans = provider.getTransaction(em);
        try {
            if (entity != null) {
                trans.begin();
                // Entity is detached at this point, so find it, then delete it
                em.remove(em.find(AnswerEntity.class, entity.getAnswerClass()));
                trans.commit();
            }
        } finally {
            if (!provider.isManaged()) {
                em.close();
            }
        }
    }

    private ManagedAnswer mapEntityToManagedAnswer(AnswerEntity entity) {
        ManagedAnswer answer = new ManagedAnswer();
        answer.setClassName(entity.getAnswerClass());
        answer.setText(entity.getAnswerText());
        answer.setType(ManagedAnswer.TypeEnum.valueOf(entity.getAnswerType().name()));
        answer.setCanonicalQuestion(entity.getCanonicalQuestion());
        return answer;
    }

    private AnswerEntity mapManagedAnswerToEntity(ManagedAnswer answer) {
        AnswerEntity entity = new AnswerEntity();
        entity.setAnswerClass(answer.getClassName());
        entity.setAnswerText(answer.getText());
        entity.setAnswerType(Answer.TypeEnum.valueOf(answer.getType().name()));
        entity.setCanonicalQuestion(answer.getCanonicalQuestion());
        return entity;
    }
}
