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

package com.ibm.watson.app.qaclassifier.selenium.drivers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 * Adds a small delay before and after web driver operations to prevent
 * timing issues that can arise when the response time between the test
 * JVM and the Selenium node is very short.
 */
public class WebDriverDelay implements WebDriverEventListener {

    private static final int DELAY_MS = Integer.valueOf(System.getProperty("selenium.delay", "100"));

    private static void delay() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void afterChangeValueOf(WebElement arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void afterClickOn(WebElement arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void afterFindBy(By arg0, WebElement arg1, WebDriver arg2) {
        delay();
    }

    @Override
    public void afterNavigateBack(WebDriver arg0) {
        delay();
    }

    @Override
    public void afterNavigateForward(WebDriver arg0) {
        delay();
    }

    @Override
    public void afterNavigateTo(String arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void afterScript(String arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void beforeChangeValueOf(WebElement arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void beforeClickOn(WebElement arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void beforeFindBy(By arg0, WebElement arg1, WebDriver arg2) {
        delay();
    }

    @Override
    public void beforeNavigateBack(WebDriver arg0) {
        delay();
    }

    @Override
    public void beforeNavigateForward(WebDriver arg0) {
        delay();
    }

    @Override
    public void beforeNavigateTo(String arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void beforeScript(String arg0, WebDriver arg1) {
        delay();
    }

    @Override
    public void onException(Throwable arg0, WebDriver arg1) {
    }

}
