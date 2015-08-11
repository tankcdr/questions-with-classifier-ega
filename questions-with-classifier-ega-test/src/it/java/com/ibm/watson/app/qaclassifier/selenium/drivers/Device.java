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

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

public enum Device {
    DESKTOP("Desktop", new DeviceConfiguration() {
        @Override
        public void configure(WebDriver driver) {
            doMaximize(driver);
        }
    }),
    TABLET("Tablet", new DeviceConfiguration() {
        @Override
        public void configure(WebDriver driver) {
            doResize(driver, 1024, 768); // iPad dimensions
        }
    }),
    IPHONE_4("iPhone 4", new DeviceConfiguration() {
        @Override
        public void configure(WebDriver driver) {
            doResize(driver, 320, 480);
        }
    }),
    IPHONE_6("iPhone 6", new DeviceConfiguration() {
        @Override
        public void configure(WebDriver driver) {
            doResize(driver, 375, 667);
        }
    });
    
    private final String name;
    private final DeviceConfiguration configurer;
    private Device(String name, DeviceConfiguration configurer) {
        this.name = name;
        this.configurer = configurer;
    }
    
    public void configure(WebDriver driver) {
        configurer.configure(driver);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    private static void doResize(WebDriver driver, int width, int height) {
        // We have run into issues where the resize command intermittently fails.
        try {
            driver.manage().window().setSize(new Dimension(width, height));
        } catch (WebDriverException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {}
            driver.manage().window().setSize(new Dimension(width, height));
        }
    }
    
    private static void doMaximize(WebDriver driver) {
        // We have run into issues where the resize command intermittently fails.
        try {
            driver.manage().window().maximize();
        } catch (WebDriverException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {}
            driver.manage().window().maximize();
        }
    }
    
    public interface DeviceConfiguration {
        public void configure(WebDriver driver);
    }
}
