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

package com.ibm.watson.app.qaclassifier.rest;

public interface ConfigurationConstants {
    /**
     * This is the threshold for calculating the confidence category (HIGH or LOW). Implementations consider a value 
     * above this threshold as HIGH, and below it LOW. Depending on the chosen categorizer, this value may be applied differently 
     * (i.e. some categorizers look at more than a single answer's confidence to determine if the threshold is reached)
     */
    public static final String HIGH_LOW_CONFIDENCE_THRESHOLD = "confidence.threshold";
    
    /**
     * Used by the CumulativeConfidenceCategorizer. This determines how many of the top answers 
     * the categorizer should use when summing their confidence values to determine if the threshold is reached.
     */
    public static final String CUMULATIVE_CONFIDENCE_COUNT = "confidence.count";
    
    /**
     * Used by the ConversationApiImpl to determine which classifier service to use. 
     * This is only applicable if two or more classifier services are bound to the app.
     * In the case of only one service, this parameter is irrelevant as we don't bother
     * checking the name and instead just use that service.
     */
    public static final String CLASSIFIER_SERVICE_NAME = "classifier.service.name";
}
