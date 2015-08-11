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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ibm.watson.app.qaclassifier.SampleQuestions;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;

@RunWith(Multiplatform.class)
public class FirstTimeUserHelpIT {
    @InjectDriver
    public WebDriver driver;
	
	@Test
	public void verifyPopupIsVisibleAndCloses() {
		
		CommonFunctions.askQuestionViaTextInput(driver, SampleQuestions.HIGH_CONFIDENCE);
		
		// Popups are animated, so we have to wait for them
		new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(driver.findElement(By.id("popup"))));
		
		assertTrue("The popup is visible upon initial home screen load",
				driver.findElement(By.id("popup")).isDisplayed());
		
		driver.findElement(By.id("close")).click();
		
		CommonFunctions.reload(driver);
		CommonFunctions.askQuestionViaTextInput(driver, SampleQuestions.HIGH_CONFIDENCE);
		
		assertFalse("The popup is not visible upon reload",
				driver.findElement(By.id("popup")).isDisplayed());
	}
}
