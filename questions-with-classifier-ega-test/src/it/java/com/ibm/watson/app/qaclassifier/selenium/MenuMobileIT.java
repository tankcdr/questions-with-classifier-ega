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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ibm.watson.app.qaclassifier.selenium.drivers.Browser;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Device;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.Platform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.Platforms;


@RunWith(Multiplatform.class)
@Platforms(
    devices = {Device.IPHONE_6, Device.IPHONE_4},
    fastPlatforms = { @Platform( browser = Browser.FIREFOX, device = Device.IPHONE_6) }
)
public class MenuMobileIT {
    @InjectDriver
    public WebDriver driver;
    
    @Test
    public void clickMenuIconShowsMenuOnMobile() {
        WebElement menuIconImg = driver.findElement(By.className("menuIconImg"));
        WebElement menuList = driver.findElement(By.className("menu-list"));
        
        // Click the menu icon, it should show the menu list
        menuIconImg.click();
        assertTrue("After clicking on the menu icon, the menu list is shown", menuList.isDisplayed());
    }
    
    @Test
    public void clickOnMenuOptionShowsContent() {
        WebElement menuIconImg = driver.findElement(By.className("menuIconImg"));
        WebElement menuList = driver.findElement(By.className("menu-list"));
        WebElement menuContent = driver.findElement(By.className("menu-content"));
        // Click the menu icon, it should show the menu list
        menuIconImg.click();
        
        //Get list of menu options
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        WebElement firstMenuOption = menuOptions.get(2); // Get content of "About" option that has content
        firstMenuOption.click();
        
        new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(menuContent));
        assertTrue("After clicking on the menu option, the menu option content is displayed", menuContent.isDisplayed());
    }
    
	@Test
    public void clickOnMenuOptionRedirectsPage() throws InterruptedException {
		WebElement menuIconImg = driver.findElement(By.className("menuIconImg"));
        WebElement menuList = driver.findElement(By.className("menu-list"));
        assertTrue("Menu Icon is displayed",
        		menuIconImg.isDisplayed());
        // Click the menu icon, it should show the menu list
        menuIconImg.click();
        
        // Unclear exactly why this is required.
        // Without this delay the findElements call might return less than the expected number of elements,
        // or some of the elements might not have any text.
        Thread.sleep(1000);
        
        //Get list of menu options
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        assertThat("Expect four menu options", menuOptions, hasSize(4));
        
        int menuIndex = 1;
        WebElement documentationOption = menuOptions.get(menuIndex); // "Documentation" option that will redirect to different page
        assertThat("Unexpected text for item at position " + menuIndex, documentationOption.getText(), is("Documentation"));
        documentationOption.click();
        
        //New tab with documentation is opened
        ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles()); //Get all existing tabs
        driver.switchTo().window(tabs.get(1)); //Access new tab
        
        assertThat("After clicking on the menu option, page is redirected",
        		driver.getTitle(), is("Natural Language Classifier service documentation | Watson Developer Cloud"));
        
        driver.close();
        driver.switchTo().window(tabs.get(0));
    }
    
}
