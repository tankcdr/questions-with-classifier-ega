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


package com.ibm.watson.app.qaclassifier;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cloudfoundry.client.lib.ClientHttpResponseCallback;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.tokens.TokensFile;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Downloads server logs from Bluemix and scans them for errors and warnings.
 * 
 * Should run after all other tests.
 *
 */
public class ScanLogs {

    public static final String LOG_FILE = "messages.log";

    @Rule
    public ErrorCollector errors = new ErrorCollector();

    @BeforeClass
    public static void getLogs() throws Exception {
        getBluemixLogs(
                System.getProperty("bluemix.user"),
                System.getProperty("bluemix.password"),
                getRequiredProperty("bluemix.server.api.url"),
                getRequiredProperty("bluemix.org"),
                getRequiredProperty("bluemix.space"),
                getRequiredProperty("bluemix.appname"));
    }

    public static void getBluemixLogs(String user, String password, String target, String org, String space, String app) throws Exception {
        CloudFoundryClient client;
        if (user == null || user.isEmpty()) {
            System.out.println("No username/password provided, using saved credentials");
            client = new CloudFoundryClient(new CloudCredentials(new TokensFile().retrieveToken(new URI(target))), new URL(target), org, space);
        } else {
            client = new CloudFoundryClient(new CloudCredentials(user, password), new URL(target), org, space);
        }

        client.openFile(app, 0, "logs/" + LOG_FILE, new ClientHttpResponseCallback() {
            @Override
            public void onClientHttpResponse(ClientHttpResponse clientHttpResponse) throws IOException {
                Path logDestination = Paths.get(LOG_FILE);
                if (Files.exists(logDestination)) {
                    Files.delete(logDestination);
                }
                Files.copy(clientHttpResponse.getBody(), logDestination);
            }
        });
    }

    private static String getRequiredProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(propertyName + " is a required property");
        }
        return value;
    }

    @Test
    public void scanLogsForWarnings() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LOG_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                errors.checkThat(line, not(containsString("WARN")));
            }
        }
    }

    @Test
    public void scanLogsForErrors() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LOG_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                errors.checkThat(line, not(containsString("ERROR")));
            }
        }
    }
}
