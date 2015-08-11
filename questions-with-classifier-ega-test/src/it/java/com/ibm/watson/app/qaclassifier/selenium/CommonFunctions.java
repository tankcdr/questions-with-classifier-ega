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

package com.ibm.watson.app.qaclassifier.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class CommonFunctions {

    /**
     * Asks a question via the text input and waits for the answer.
     * 
     * @param driver
     * @param questionText
     */
    public static void askQuestionViaTextInput(WebDriver driver, String questionText) {
        driver.findElement(By.id("questionInputField")).sendKeys(questionText + "\n");
        waitForAnswer(driver);
    }

    /**
     * Asks a question via the text input and does not wait for the answer.
     * 
     * @param driver
     * @param questionText
     */
    public static void askQuestionViaTextInputExpectingError(WebDriver driver, String questionText) {
        driver.findElement(By.id("questionInputField")).sendKeys(questionText + "\n");
    }

    /**
     * Asks a question by clicking the first top question.
     * 
     * @param driver
     * @return The text of the first top question
     */
    public static String askQuestionViaTopQuestions(WebDriver driver) {
        WebElement topQuestion = driver.findElement(By.id("top-question-0"));
        String questionText = topQuestion.getText();
        topQuestion.click();

        waitForAnswer(driver);

        return questionText;
    }

    /**
     * Waits until the application is ready for user interaction.
     * 
     * @param driver
     */
    public static void waitForApp(WebDriver driver) {
        // Question input field is not enabled until the startConversation API request returns.
        new WebDriverWait(driver, 10).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return input.findElement(By.id("questionInputField")).isEnabled();
            }
        });
    }

    /**
     * Reloads the application and waits until it is ready for user interaction.
     * 
     * @param driver
     */
    public static void reload(WebDriver driver) {
        driver.navigate().refresh();
        waitForApp(driver);
    }

    public static boolean isMobileUI(WebDriver driver) {
        return driver.manage().window().getSize().getWidth() <= 767;
    }

    public static boolean isTabletUI(WebDriver driver) {
        int width = driver.manage().window().getSize().getWidth();
        return width > 767 && width <= 1199;
    }

    public static String getBrowserName(WebDriver driver) {
        while (driver instanceof WrapsDriver) {
            driver = ((WrapsDriver) driver).getWrappedDriver();
        }
        return ((HasCapabilities) driver).getCapabilities().getBrowserName();
    }

    private static void waitForAnswer(WebDriver driver) {
        // We know that an answer has been returned when the answer tag has a child element.
        new WebDriverWait(driver, 10).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return input.findElements(By.xpath("//answer/*")).size() > 0;
            }
        });
    }

}
