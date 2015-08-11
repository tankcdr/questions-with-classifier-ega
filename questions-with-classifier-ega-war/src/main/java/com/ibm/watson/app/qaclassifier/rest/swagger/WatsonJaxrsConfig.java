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

package com.ibm.watson.app.qaclassifier.rest.swagger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.config.WebXMLReader;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Swagger;

public class WatsonJaxrsConfig extends DefaultJaxrsConfig {
    private static final Logger logger = LogManager.getLogger();
    private static final long serialVersionUID = 4760955464533876613L;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        Info info = null;        
        try(InputStream swaggerJsonStream = getClass().getClassLoader().getResourceAsStream("swagger.json")) {
            if(swaggerJsonStream != null) {
                // Lets add in some info from swagger.json            
                final JsonParser parser = new JsonParser();
                JsonElement json = parser.parse(new InputStreamReader(swaggerJsonStream, StandardCharsets.UTF_8));      
                info = new Gson().fromJson(json.getAsJsonObject().get("info"), Info.class); 
            }                      
        } catch (Exception e) {
            // Couldn't add any info in, oh well
            logger.debug("Couldn't add in swagger.json info", e);
        }
        
        registerReaderWithInfo(servletConfig, info);
        
        // WebXML reader sets this, override it here
        ScannerFactory.setScanner(new DefaultJaxrsScanner() {
            @Override
            public Set<Class<?>> classesFromContext(Application app, ServletConfig sc) {
                Set<Class<?>> retval = super.classesFromContext(app, sc);
                for(Object o : app.getSingletons()) {
                    retval.add(o.getClass());
                }
                return retval;
            }
        });
    }
    
    private void registerReaderWithInfo(ServletConfig servletConfig, final Info info) {
        WebXMLReader reader = new WebXMLReader(servletConfig){
            @Override
            public Swagger configure(Swagger swagger) {
                swagger = super.configure(swagger);
                if(info != null) {
                    Info oldInfo = swagger.getInfo();
                    swagger.setInfo(info.mergeWith(oldInfo));
                }
                return swagger;
            }
        };
        
        servletConfig.getServletContext().setAttribute("reader", reader);
    }
}
