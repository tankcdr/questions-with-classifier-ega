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

package com.ibm.watson.app.qaclassifier.selenium;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ibm.watson.app.qaclassifier.SampleQuestions;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;

@RunWith(Multiplatform.class)
public class HighLowConfidenceAnswersIT {
    @InjectDriver
    public WebDriver driver;
    
    @Test
    public void askHighConfidenceQuestionViaTextInput() {
        String questionText = SampleQuestions.HIGH_CONFIDENCE;
        CommonFunctions.askQuestionViaTextInput(driver, questionText);

        assertThat("After asking question via text input, found expected question text on answer page",
                getDisplayedQuestionText(), containsString(questionText));

        assertTrue("After asking question via text input, found answer text on answer page",
                getDisplayedAnswerText().length() > 0);
        
        assertTrue("After a high confidence answer has been received, the positive feedback button is shown",
        		driver.findElement(By.id("positiveFeedbackInput")).isDisplayed());
        
        assertTrue("After a high confidence answer has been received, the negative feedback button is shown",
        		driver.findElement(By.id("negativeFeedbackInput")).isDisplayed());
    }
    
    @Test
    public void askLowConfidenceQuestionViaTextInput() {
        CommonFunctions.askQuestionViaTextInput(driver, SampleQuestions.LOW_CONFIDENCE);

        assertThat("After asking question via text input, find expected question text on answer page",
                getDisplayedQuestionText(), containsString(SampleQuestions.LOW_CONFIDENCE));

        assertTrue("After asking question via text input, find low confidence prompt on page",
        		findNoneOfTheAboveButton());
    }
    
    @Test
    public void askNoAnswerQuestion() {
        CommonFunctions.askQuestionViaTextInput(driver, SampleQuestions.NO_ANSWERS);

        assertThat("After asking question via text input, find expected question text on answer page",
        		driver.findElement(By.id("questionTitle")).getText(), containsString(SampleQuestions.NO_ANSWERS));

        assertTrue("After asking question via text input, find the forum button in Still Need Help section",
                visitForumButtonFound());
    }

    private String getDisplayedQuestionText() {
        return driver.findElement(By.className("question-text")).getText();
    }

    private String getDisplayedAnswerText() {
        return driver.findElement(By.className("answer-quote")).getText();
    }
    
    private Boolean findNoneOfTheAboveButton() {
    	return driver.findElement(By.className("none")).isDisplayed();
    }
    
    private Boolean visitForumButtonFound() {
    	return driver.findElement(By.className("visitForum")).isDisplayed();
    }
}
