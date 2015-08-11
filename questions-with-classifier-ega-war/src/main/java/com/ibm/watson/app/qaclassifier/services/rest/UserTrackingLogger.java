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

import java.util.Date;
import java.util.List;

import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;


/**
 * Interface for classes that log user-tracking events.
 *
 * In addition to tracking the events (query, impression, and click),
 * the logger must also keep track of enough state to map impressions
 * back to the query that generated them, so those impressions can be
 * looked up later if they are clicked.
 */
public interface UserTrackingLogger {

    public long getPositiveImpressionIdForMessage(long messageId);

    public long getNegativeImpressionIdForMessage(long messageId);

    public long getImpressionIdForMessage(long messageId, String questionText);
    
    public long getTopQuestionImpressionId(long conversationId, String questionText);

    public long getNoneOfTheAboveImpressionIdForMessage(long messageId);

    public long getForumVisitImpressionIdForMessage(long messageId);

    /**
     * Return a list of all impressions for the given message ID that have location=HIDDEN.
     * 
     * @param messageId
     * @return
     */
    public List<Long> getHiddenImpressionIdsForMessage(long messageId);

    /**
     * Log a user query event.
     * 
     * @param timestamp    The time the event occurred
     * @param user         Some identifier that ideally represents a single user interacting with the system
     * @param hostname     The hostname or identifier of the system
     * @param referral     The ID of the click that triggered this query, or null
     * @param questionText The text of the query
     * @param mode         How the query was input by the user
     * @return             The event ID
     */
    public long query(Date timestamp, String user, String hostname, Long referral, String questionText, InputMode mode);

    /**
     * Log a user impression event
     * 
     * @param timestamp       The time the event occurred
     * @param user            Some identifier that ideally represents a single user interacting with the system
     * @param hostname        The hostname or identifier of the system
     * @param referral        The ID of the query that triggered this impression, or null
     * @param prompt          The impression text
     * @param classifiedClass The answer class from the classifier, or null
     * @param offset          The 1-based offset of this impression relative to other impressions from the same query
     * @param action          Currently not used
     * @param location        The location of the impression on the screen
     * @param provenance      {@see Provenance}
     * @param confidence      The confidence score from the classifier, or 0
     * @return                The event ID
     */
    public long impression(Date timestamp, String user, String hostname, Long referral, String prompt,
            String classifiedClass, int offset, String action, Location location, Provenance provenance, double confidence);

    /**
     * @param timestamp The time the event occurred
     * @param user      Some identifier that ideally represents a single user interacting with the system
     * @param hostname  The hostname or identifier of the system
     * @param referral  The ID of the impression that was clicked
     * @return          The event ID
     */
    public long click(Date timestamp, String user, String hostname, Long referral);

    /**
     * Unhides an impression that was previously hidden from the user.
     * The event ID of the impression is unchanged.
     * 
     * @param impressionId The event ID of the impression
     * @param newLocation  The location of the impression on the screen
     */
    public void unhideImpression(long impressionId, Location newLocation);

    /**
     * Possible impression locations on the page
     */
    public static enum Location {
        MAIN_RESULTS_AREA, RIGHT_RAIL, FOOTER, 
        /**
         * This is a special location to indicate impressions that were generated from a query
         * but not initially shown to the user.  When the impression is shown to the user it
         * should be unhidden via unhideImpression().
         */
        HIDDEN
    };

    /**
     * Where an impression came from.
     * Currently only used to distinguish between clickable and non-clickable impressions.
     */
    public static enum Provenance {
        /**
         * This impression shows an answer inline and is not clickable.
         */
        INLINE, 
        /**
         * This impression is clickable to either provide feedback or submit another query.
         */
        ALGO
    };

}
