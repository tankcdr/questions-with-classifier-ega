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
package com.ibm.watson.app.qaclassifier.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

import org.apache.commons.io.IOUtils;

public class FileUtils {
    public static String loadFromFilesystemOrClasspath(String path) throws IOException {
        return loadFromFilesystemOrClasspath(path, StandardCharsets.UTF_8);
    }

    public static String loadFromFilesystemOrClasspath(String path, Charset charset) throws IOException {
        String contents = null;

        File file = new File(path);
        if (file.exists()) {
            contents = org.apache.commons.io.FileUtils.readFileToString(file, charset);
            if (contents != null) {
                System.out.println(MessageKey.AQWQAC10100I_reading_from_file_1.getMessage(file.getAbsolutePath()).getFormattedMessage());
            }
        }

        if (contents == null) {
            // Try the classpath
            try (InputStream stream = FileUtils.class.getResourceAsStream(path)) {
                contents = IOUtils.toString(stream, charset);
                if (contents != null) {
                    System.out.println(MessageKey.AQWQAC10101I_reading_from_classpath_1.getMessage(path).getFormattedMessage());
                }
            }
        }

        if (contents == null) {
            throw new IllegalArgumentException(MessageKey.AQWQAC14101E_could_not_find_path_on_classpath_1.getMessage(path).getFormattedMessage());
        }

        return contents;
    }
}
