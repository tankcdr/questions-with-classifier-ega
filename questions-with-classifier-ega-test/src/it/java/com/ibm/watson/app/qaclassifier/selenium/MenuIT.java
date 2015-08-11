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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.ibm.watson.app.qaclassifier.selenium.drivers.Browser;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Device;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.Platform;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.Platforms;

@RunWith(Multiplatform.class)
@Platforms(
    devices = {Device.DESKTOP, Device.TABLET},
    fastPlatforms = {
            @Platform(browser = Browser.CHROME, device = Device.DESKTOP),
            @Platform(browser = Browser.IE, device = Device.DESKTOP),
            @Platform(browser = Browser.FIREFOX, device = Device.TABLET)
    }
)
public class MenuIT {
    @InjectDriver
    public WebDriver driver;
    
    @Before
    public void removeTimeout() {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    }
    
    @Test
    public void clickMenuIconShowsMenu() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        // Click the menu icon, it should show the menu list
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        assertTrue("After clicking on the menu icon, the menu list is shown", menuList.isDisplayed());
    }
    
    @Test
    public void clickMenuIconTwiceShowsAndHidesMenu() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.tagName("menu-list"));
        assertTrue("After clicking the menu icon, the menu list is visible", menuList.isDisplayed());
        
        // Click the menu icon again, the menu list should go away
        menuIconImg.click();
        assertFalse("Clicking on the menu icon again closes the menu list", menuList.isDisplayed());
    }
    
    // Can't get this working in IE, so disable until the feature is reenabled
    @Ignore @Test
    public void clickMenuIconThenAwayFromMenuIconHidesMenuList() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        // Clicking away from the menu icon should also close the menu
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.tagName("menu-list"));
        assertTrue("After clicking the menu icon, the menu list is visible", menuList.isDisplayed());
        
        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.tagName("body")), 0, 0);
        action.click().build().perform();
        assertFalse("After clicking outside the menu icon, the menu list is gone", menuList.isDisplayed());
    }
    
    @Test
    public void validateMenuListHasOptions() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        // Validate that the menu list has options
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        assertTrue("The menu list has options", menuOptions.size() > 0);
    }
    
    @Test
    public void clickingOnMenuListItemShowsOverlay() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        // Validate that the menu list has options
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        assertTrue("After clicking the menu icon, the menu list is visible", menuList.isDisplayed());
        
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        
        // The first item should be home, which is hidden on Desktop, so skip it and do the second one
        WebElement firstMenuOption = menuOptions.get(2);
        String firstMenuOptionText = firstMenuOption.getText();
        firstMenuOption.click();
        assertTrue("Clicking on a menu option keeps the menu list open", menuList.isDisplayed());
        
        WebElement menuOverlay = driver.findElement(By.id("menuOverlay"));
        WebElement menuOverlayHeading = menuOverlay.findElement(By.xpath(".//div[@class='header']/h2"));
        assertThat("The menu list option contains the same text as the menu overlay heading", 
                firstMenuOptionText, is(menuOverlayHeading.getText()));
        WebElement menuOverlayContent = menuOverlay.findElement(By.id("menuContent"));
        assertTrue("The menu overlay contains some text content for the selected menu option", menuOverlayContent.getText().length() > 0);
    }
    
    @Test
    public void closeMenuOverlayByClickingOnClose() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        
        // The first item should be home, which is hidden on Desktop, so skip it and do the second one
        WebElement firstMenuOption = menuOptions.get(2);
        firstMenuOption.click();
        
        // Hit ESC closes the menu overlay
        WebElement menuOverlay = driver.findElement(By.id("menuOverlay"));
        WebElement close = menuOverlay.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' close ')]"));
        close.click();
        assertTrue("Hitting the close button closes the menu overlay", driver.findElements(By.id("menuOverlay")).size() == 0);
    }
    
    @Test
    public void closeMenuOverlayWithEscapeKey() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        
        // The first item should be home, which is hidden on Desktop, so skip it and do the second one
        WebElement firstMenuOption = menuOptions.get(2);
        firstMenuOption.click();
        
        // Press ESC to close overlay
        WebElement menuOverlay = driver.findElement(By.id("menuOverlay"));   
        menuOverlay.sendKeys(Keys.ESCAPE);
        assertTrue("Hitting the ESC key closes the menu overlay", driver.findElements(By.id("menuOverlay")).size() == 0);
    }
    
    @Test
    public void closeMenuOverlayByClickingOutsideOfContent() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        
        // The first item should be home, which is hidden on Desktop, so skip it and do the second one
        WebElement firstMenuOption = menuOptions.get(2);
        firstMenuOption.click();
        
        // Click outside of the content
        WebElement menuOverlay = driver.findElement(By.id("menuOverlay")); 
        WebElement menuContent = menuOverlay.findElement(By.xpath(".//div[@id='menu']"));
        Actions action = new Actions(driver);
        action.moveToElement(menuContent, -1, -1);
        action.click().build().perform();
        assertTrue("Clicking outside of the content closes the menu overlay", driver.findElements(By.id("menuOverlay")).size() == 0);
    }
    
    @Test
    public void clickingOnMenuContentDoesNotCloseOverlay() {
        WebElement menuIconContainer = driver.findElement(By.id("menuIconContainerDesktop"));
        WebElement menuIconImg = menuIconContainer.findElement(By.xpath(".//span[contains(concat(' ', @class, ' '), ' menuIconImg ')]"));
        
        menuIconImg.click();
        WebElement menuList = driver.findElement(By.className("menu-list"));
        List<WebElement> menuOptions = menuList.findElements(By.xpath(".//ul/li"));
        
        // The first item should be home, which is hidden on Desktop, so skip it and do the second one
        WebElement firstMenuOption = menuOptions.get(2);
        firstMenuOption.click();
        
        // Click inside of the content
        WebElement menuOverlay = driver.findElement(By.id("menuOverlay")); 
        WebElement menuContent = menuOverlay.findElement(By.xpath(".//div[@id='menu']"));
        Actions action = new Actions(driver);
        action.moveToElement(menuContent, 1, 1);
        action.click().build().perform();
        assertTrue("Clicking inside of the content does not close the menu overlay", driver.findElements(By.id("menuOverlay")).size() == 1);
    }
}
