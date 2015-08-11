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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;

@RunWith(Multiplatform.class)
public class HomePageIT {
    @InjectDriver
    public WebDriver driver;
    
    @Test
    public void pageLoadsSuccessfully() {
        assertEquals("The page title should be correct on load.",
                "Showcase App for NL Classifier",
                driver.getTitle());
    }
    
    @Test
    public void appHeadingIsPresent() {
        String headingText = driver.findElement(By.id("app-heading")).getText();

        assertThat("Heading text is present and correct", headingText, is("Questions on the Natural Language Classifier"));
    }

    @Test
    public void askQuestionViaTopQuestions() {
        String questionText = CommonFunctions.askQuestionViaTopQuestions(driver);

        assertThat("After asking first top question, didn't find expected top question text on answer page",
                getDisplayedQuestionText(), containsString(questionText));

        assertTrue("After asking first top question, didn't find any answer text on answer page",
                getDisplayedAnswerText().length() > 0);
    }

    @Test
    public void askQuestionContainingOnlySpaces() {
        assumeFalse("Defect 106427", CommonFunctions.isMobileUI(driver) || CommonFunctions.isTabletUI(driver));

        CommonFunctions.askQuestionViaTextInputExpectingError(driver, "     ");
        WebElement errorText = driver.findElement(By.id("questionValidationError"));
        assertTrue("Error message was not displayed after asking an invalid question", errorText.isDisplayed());
        assertThat("Incorrect error message", errorText.getText(), is("A question must contain at least one word"));
    }

    private String getDisplayedQuestionText() {
        return driver.findElement(By.className("question-text")).getText();
    }

    private String getDisplayedAnswerText() {
        return driver.findElement(By.className("answer-quote")).getText();
    }
}
