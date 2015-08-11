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

package com.ibm.watson.app.qaclassifier.rest.impl;

import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.ibm.watson.app.qaclassifier.rest.FeedbackApiInterface;
import com.ibm.watson.app.qaclassifier.rest.NotFoundException;
import com.ibm.watson.app.qaclassifier.rest.model.Feedback;
import com.ibm.watson.app.qaclassifier.services.rest.UserTracking;

public class FeedbackApiImpl implements FeedbackApiInterface {
    
    private UserTracking tracking;
    
    @Inject
    public FeedbackApiImpl(UserTracking tracking) {
        this.tracking = tracking;
    }

    @Override
    public Response feedback(Feedback feedback) throws NotFoundException {
        switch (feedback.getAction()) {
            case HELPFUL:
                tracking.answerWasHelpful(feedback.getConversationId(), feedback.getMessageId());
                break;
            case UNHELPFUL:
                tracking.answerWasUnhelpful(feedback.getConversationId(), feedback.getMessageId());
                break;
            case NO_HELPFUL_REFINEMENTS:
                tracking.questionRefinementsWereUnhelpful(feedback.getConversationId(), feedback.getMessageId());
                break;
            case FORUM_REDIRECT:
                tracking.visitedTheForum(feedback.getConversationId(), feedback.getMessageId());
                break;
        }
        return null;
    }

}
