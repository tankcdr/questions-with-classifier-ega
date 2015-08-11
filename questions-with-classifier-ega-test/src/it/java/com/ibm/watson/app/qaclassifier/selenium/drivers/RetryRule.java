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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import com.ibm.watson.app.qaclassifier.selenium.CommonFunctions;

/**
 * Retries failing Selenium tests
 */
public class RetryRule implements TestRule {

    private static final int RETRY_COUNT = Integer.valueOf(System.getProperty("selenium.retries", "1"));

    private final AtomicReference<WebDriver> driverRef;

    public RetryRule(AtomicReference<WebDriver> driverRef) {
        this.driverRef = driverRef;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Throwable caughtFailure = null;
                for (int i = 0; i < RETRY_COUNT; i++) {
                    try {
                        if (i > 0) {
                            System.err.println("Retry " + i + " for " + description);
                            CommonFunctions.reload(driverRef.get());
                        }
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        logRetry(t, description);
                        caughtFailure = t;
                    }
                }
                throw caughtFailure;
            }
        };
    }

    private void logRetry(Throwable t, Description description) {
        System.err.println(description + " failed due to " + ExceptionUtils.getFullStackTrace(t));
    }

}
