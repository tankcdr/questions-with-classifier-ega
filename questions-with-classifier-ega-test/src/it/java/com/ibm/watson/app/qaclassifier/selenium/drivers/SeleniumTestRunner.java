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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.google.common.io.Files;
import com.ibm.watson.app.qaclassifier.selenium.CommonFunctions;
import com.ibm.watson.app.qaclassifier.selenium.drivers.Multiplatform.InjectDriver;

public class SeleniumTestRunner extends BlockJUnit4ClassRunner {
    private Environment env;
    private Device device;
    private Browser browser;
    
    private final AtomicReference<WebDriver> driverRef = new AtomicReference<>();
    
    public SeleniumTestRunner(TestClass testClass, Environment env, Device device, Browser browser) throws InitializationError {
        super(testClass.getJavaClass());
        this.env = env;
        this.device = device;
        this.browser = browser;
    }       
    
    @Override
    protected String getName() {
        // Don't use parens, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512
        return getTestClass().getName() + " [" + device + "," + browser + "]";
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return method.getName() + "(" + device + "," + browser + ")";
    }
    
    @Override
    protected void validateFields(List<Throwable> errors) {
        super.validateFields(errors);
        validateWebDriverFieldPublicNotStatic(errors);
    }
    
    private void validateWebDriverFieldPublicNotStatic(List<Throwable> errors) {
        List<FrameworkField> fields = getTestClass().getAnnotatedFields(InjectDriver.class);
        if(fields.size() == 0) {
            errors.add(new Exception("No field annotated with @InjectDriver"));
            return;
        }
        if(fields.size() != 1) {
            errors.add(new Exception("More than one field annotated with @InjectDriver"));
            return;
        }
        
        FrameworkField field = fields.get(0);
        if(!field.isPublic() || field.isStatic()) {
            errors.add(new Exception("Field annotated with @InjectDriver must be public and not static"));
        }
        
        if(!field.getType().isAssignableFrom(WebDriver.class)) {
            errors.add(new Exception("Field annotated with @InjectDriver must be an instance of WebDriver"));
        }
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        if (this.driverRef.get() == null) {
            WebDriver driver = getDriver(env, device, browser);
            this.driverRef.set(driver);
        }
        getDriverField().getField().set(test, this.driverRef.get());
        return test;
    }
    
    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = super.getTestRules(target);
        
        // Load the app and potentially skip the welcome screen
        rules.add(new SkipWelcomeScreenRule(driverRef, env, getTestClass().getAnnotation(SeleniumConfig.class)));
        
        // Take a screenshot after each test
        rules.add(new ScreenshotRule(driverRef));
        
        // Retry any tests that fail
        rules.add(new RetryRule(driverRef));
        
        return rules;
    }
    
    @Override
    protected List<TestRule> classRules() {
        List<TestRule> classRules = super.classRules();
        classRules.add(new CloseDriverRule(driverRef));
        return classRules;
    }
    
    private FrameworkField getDriverField() {
        return getTestClass().getAnnotatedFields(InjectDriver.class).get(0);
    }
    
    private WebDriver getDriver(Environment env, Device device, Browser browser) throws Exception {
        WebDriver driver = browser.getDriver(env);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        // Add a small delay to prevent timing issues in the browser
        EventFiringWebDriver driverWithEvents = new EventFiringWebDriver(driver);
        driverWithEvents.register(new WebDriverDelay());
        
        device.configure(driverWithEvents);
        return driverWithEvents;
    }
    
    private static class CloseDriverRule extends ExternalResource {
        private final AtomicReference<WebDriver> driverRef;
        private CloseDriverRule(AtomicReference<WebDriver> driverRef) {
            this.driverRef = driverRef;
        }
        
        @Override
        protected void after() {
            WebDriver driver = driverRef.get();
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
    }
    
    private static class ScreenshotRule extends TestWatcher {
        private final AtomicReference<WebDriver> driverRef;
        
        private ScreenshotRule(AtomicReference<WebDriver> driverRef) {
            this.driverRef = driverRef;
        }
        
        @Override
        protected void succeeded(Description description) {
            if (CommonFunctions.getBrowserName(driverRef.get()).equals(BrowserType.IE)) {
                // Screenshots are slow on IE
                return;
            } else {
                takeScreenShot(getOutputFile(description, true));
            }
        }
        
        @Override
        protected void failed(Throwable e, Description description) {
            takeScreenShot(getOutputFile(description, false));
        }
        
        private File getOutputFile(Description description, boolean passed) {
            File outputDir = new File("target/screenshots", description.getClassName());
            outputDir.mkdirs();
            String result = passed ? "PASSED" : "FAILED";
            String timestamp = System.getenv("BUILD_TAG") == null ? "" : "_" + System.currentTimeMillis();
            return new File(outputDir, description.getMethodName() + "_" + result + timestamp + ".png");
        }
        
        private void takeScreenShot(File outputFile) {
            WebDriver driver = driverRef.get();
            while (driver instanceof WrapsDriver) {
                driver = ((WrapsDriver) driver).getWrappedDriver();
            }
            TakesScreenshot augmentedDriver = (TakesScreenshot) new Augmenter().augment(driver);
            try {
                Files.write(augmentedDriver.getScreenshotAs(OutputType.BYTES), outputFile);
                if (System.getenv("BUILD_TAG") != null) {
                    // For the Jenkins JUnit Attachment Plugin
                    System.out.println("[[ATTACHMENT|" + outputFile.getAbsolutePath() + "]]");
                } else {
                    System.out.println("Saved a screenshot to " + outputFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
