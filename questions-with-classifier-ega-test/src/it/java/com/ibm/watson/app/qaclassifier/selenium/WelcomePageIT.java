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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;
import com.ibm.watson.app.qaclassifier.selenium.drivers.SeleniumConfig;

@RunWith(Multiplatform.class)
@SeleniumConfig(skipWelcomeScreen = false)
/*
 * Test ordering is important because pressingNextButtonDynamic will dismiss the welcome screen.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WelcomePageIT {
    @InjectDriver
    public WebDriver driver;

	@Test
	public void pageLoadsSuccessfully() throws Exception {
        assertEquals("The page title should be correct on load.",
        "Showcase App for NL Classifier",
        driver.getTitle());
        assertTrue("The welcome screen is visible", driver.findElement(By.tagName("welcome")).isDisplayed());
	}

	@Test
	public void pressingNextButtonDynamic() throws InterruptedException {
	    
		WebElement screenText   = driver.findElement(By.id("screenText"));
		WebElement trainMeText  = driver.findElement(By.id("trainMeText"));
		WebElement actionButton = driver.findElement(By.id("actionButton"));
		
		assertEquals("Proper welcome text is displayed.", 
		        "Hello! I'm Watson. I'm learning to answer questions about the Natural Language Classifier.", 
		        screenText.getText().replaceAll("\\s+", " "));
		
		assertEquals("Proper train me text is displayed.", 
				"Train me. Let me know whether I provide good answers.", 
		        trainMeText.getText().replaceAll("\\s+", " "));
		
		actionButton.click();
		
		assertTrue("Home page loads after pressing next button second time",
				driver.findElements(By.className("home")).size() > 0);
	}
}
