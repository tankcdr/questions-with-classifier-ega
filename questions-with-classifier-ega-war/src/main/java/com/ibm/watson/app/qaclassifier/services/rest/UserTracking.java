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

package com.ibm.watson.app.qaclassifier.services.rest;

import java.util.List;

import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Question;

public interface UserTracking {
    
    /*
     * Impression prompt text for feedback buttons.
     */
    public static final String VISIT_THE_FORUM = "Visit the forum";
    public static final String NONE_OF_THE_ABOVE = "None of the above";
    public static final String I_STILL_NEED_HELP = "I still need help";
    public static final String THIS_WAS_HELPFUL = "this was helpful";

    /**
     * Called when the user submits a question.
     * 
     * The question could come from a number of sources:
     *   1) Typed by the user
     *   2) Clicked by the user from a list of top/suggested questions
     *   3) Clicked by the user from a list of refinement/did-you-mean questions
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param messageId      In case 3, set to the current message ID.  Otherwise null.
     * @param questionText   The question text submitted by the user.
     * @param mode           How the question was submitted.
     * @param answers        The answers to the question.
     * 
     * @return The new message id
     */
    public String questionAsked(String conversationId, String messageId, String questionText, InputMode mode, List<Answer> answers);
        
    /**
     * Called when the user submits feedback indicating that the provided answer was helpful.
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param messageId      The message ID associated with the response for which feedback was provided
     */
    public void answerWasHelpful(String conversationId, String messageId);
    
    /**
     * Called when the user submits feedback indicating that the provided answer was not helpful.
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param messageId      The message ID associated with the response for which feedback was provided
     */
    public void answerWasUnhelpful(String conversationId, String messageId);
    
    /**
     * Called when the user submits feedback indicating that the provided question refinements/suggestions were not helpful.
     * Appears in the UI as "None of the above".
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param messageId      The message ID associated with the response for which feedback was provided
     */
    public void questionRefinementsWereUnhelpful(String conversationId, String messageId);
    
    /**
     * Called when the user submits feedback indicating that they would like to visit the forum.
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param messageId      The message ID associated with the response for which feedback was provided
     */
    public void visitedTheForum(String conversationId, String messageId);
    
    /**
     * Called when the application displays the top questions list to the user for the first time.
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param topQuestions   The TopQuestionList containing the top questions displayed to the user
     */
    public void displayedTopQuestions(String conversationId, List<Question> topQuestions);
    
    /**
     * 
     * @param conversationId The conversation ID tracking all of the user's interactions with the application
     * @param topQuestions   The TopQuestionList containing the top questions displayed to the user
     */
    public void redisplayedTopQuestions(String conversationId, List<Question> topQuestions);
    
    /**
     * Whether a query was submitted by a text input or a click
     */
    public static enum InputMode { TYPED, CLICKED };
}
