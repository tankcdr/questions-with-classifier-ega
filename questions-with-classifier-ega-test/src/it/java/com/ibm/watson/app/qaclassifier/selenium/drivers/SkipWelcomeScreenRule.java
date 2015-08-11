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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.rules.ExternalResource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ibm.watson.app.qaclassifier.selenium.CommonFunctions;

public class SkipWelcomeScreenRule extends ExternalResource {
    private final AtomicReference<WebDriver> driverRef;
    private final Environment env;
    private final SeleniumConfig config;
    
    private static Set<WebDriver> didSkipWelcomeScreen = new HashSet<>();
    
    public SkipWelcomeScreenRule(AtomicReference<WebDriver> driverRef, Environment env, SeleniumConfig config) {
        this.driverRef = driverRef;
        this.env = env;
        this.config = config == null ? getDefaultConfig() : config;
    }
    
    private SeleniumConfig getDefaultConfig() {
        // This is a bit of a hack, but we need annotations for JUnit
        // So this is a way to store default values when no annotation is present
        return new SeleniumConfig() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return SeleniumConfig.class;
            }

            @Override
            public boolean skipWelcomeScreen() {
                return true;
            }            
        };
    }
    
    @Override
    protected void before() throws Throwable {
        WebDriver driver = driverRef.get();
        driver.get(env.getAppUrl());
        if(config.skipWelcomeScreen()) {
            if (didSkipWelcomeScreen.add(driver)) {
                skipWelcomeScreen();
            }
            CommonFunctions.waitForApp(driver);
        }
    }
    
    private void skipWelcomeScreen() throws InterruptedException {
        WebDriver driver = driverRef.get();
        // In some cases on Internet Explorer it can take more than five seconds for this to appear.
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.id("actionButton")));
        WebElement actionButton = driver.findElement(By.id("actionButton"));
        actionButton.click();
    }
}
