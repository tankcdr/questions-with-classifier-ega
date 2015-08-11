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

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public enum Browser {
    IE(internetExplorerCapabilities(), new LocalWebDriverFactory() {
        @Override
        public WebDriver get(Environment env) {
            return new InternetExplorerDriver();
        }
    }), 
    FIREFOX(DesiredCapabilities.firefox(), new LocalWebDriverFactory() {
        @Override
        public WebDriver get(Environment env) {
            return new FirefoxDriver();
        }
    }), 
    CHROME(DesiredCapabilities.chrome(), new LocalWebDriverFactory() {
        @Override
        public WebDriver get(Environment env) {
            return new ChromeDriver();
        }
    });
    
    private final DesiredCapabilities capabilities;
    private final LocalWebDriverFactory localDriverFactory;
    private Browser(DesiredCapabilities capabilities, LocalWebDriverFactory localDriverFactory) {
        this.capabilities = capabilities;
        this.localDriverFactory = localDriverFactory;
    }
    
    private static DesiredCapabilities internetExplorerCapabilities() {
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        // Required to display welcome screen
        capabilities.setCapability("ie.ensureCleanSession", true);
        return capabilities;
    }

    public WebDriver getDriver(Environment env) throws MalformedURLException {
        if(env == Environment.REMOTE) {
            URL url = new URL(env.getGridUrl());
            return getRemoteDriverWithRetries(url);
        } else {
            // Local
            return localDriverFactory.get(env);
        }
    }

    private RemoteWebDriver getRemoteDriverWithRetries(URL url) {
        WebDriverException exception = null;
        for (int i = 0; i < 5; i++) {
            try {
                return new RemoteWebDriver(url, capabilities);
            } catch (WebDriverException e) {
                // Retry in case of a flaky grid node
                e.printStackTrace();
                exception = e;
            }
        }
        throw exception;
    }
    
    @Override
    public String toString() {
        return capabilities.getBrowserName();
    }
    
    public interface LocalWebDriverFactory {
        public WebDriver get(Environment env);
    };
}




