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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

public class Multiplatform extends Suite {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Platforms {
        Environment[] envs() default Environment.REMOTE;
        Device[] devices() default { Device.DESKTOP, Device.IPHONE_6, Device.IPHONE_4, Device.TABLET };
        Browser[] browsers() default { Browser.CHROME, Browser.FIREFOX, Browser.IE };
        
        Platform[] fastPlatforms() default { 
            @Platform(browser = Browser.CHROME, device = Device.DESKTOP),
            @Platform(browser = Browser.IE, device = Device.DESKTOP),
            @Platform(browser = Browser.FIREFOX, device = Device.IPHONE_6),
            @Platform(browser = Browser.FIREFOX, device = Device.TABLET)
        };
    }
    
    public @interface Platform {
        Device device();
        Browser browser();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InjectDriver {
    }    
    
    private final List<Runner> runners;

    public Multiplatform(Class<?> klass) throws InitializationError {
        super(klass, Collections.<Runner>emptyList());
        
        Platforms annotation = getTestClass().getAnnotation(Platforms.class);
        if (annotation == null) {
            annotation = DefaultPlatforms.class.getAnnotation(Platforms.class);
        }

        String runFullSuite = System.getProperty("selenium.full.suite");
        if (runFullSuite != null && runFullSuite.equalsIgnoreCase("true")) {
            runners = Collections.unmodifiableList(createRunners(annotation.envs(), annotation.devices(), annotation.browsers()));
        }
        else {
            runners = Collections.unmodifiableList(createRunners(annotation.fastPlatforms()));
        }
    }
    
    private List<Runner> createRunners(Platform[] platforms) throws InitializationError {
        List<Runner> runners = new ArrayList<>();
        for (Platform platform : platforms) {
            runners.add(new SeleniumTestRunner(getTestClass(), Environment.REMOTE, platform.device(), platform.browser()));
        }
        
        // Randomize the order to help things run a bit faster in parallel
        Collections.shuffle(runners);
        return runners;
    }
    
    private List<Runner> createRunners(Environment[] envs, Device[] devices, Browser[] browsers) throws InitializationError {
        List<Runner> runners = new ArrayList<>();

        for(Environment env : envs) {
            for(Device device : devices) {
                for(Browser browser : browsers) {
                    if (browser == Browser.IE && device != Device.DESKTOP) {
                        // IE tablet and mobile not supported
                        continue;
                    }
                    runners.add(new SeleniumTestRunner(getTestClass(), env, device, browser));
                }
            }
        }

        // Randomize the order to help things run a bit faster in parallel
        Collections.shuffle(runners);
        return runners;
    }
    
    @Override
    public List<Runner> getChildren() {
        return runners;
    }
    
    @Platforms
    private class DefaultPlatforms {}
}
