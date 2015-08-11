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

package com.ibm.watson.app.qaclassifier.services.answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.watson.app.common.services.nlclassifier.model.NLClassifierTrainingData;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassifierTrainingData.TrainingInstance;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class TrainingJsonCanonicalQuestionService implements CanonicalQuestionService {
    private static final Logger logger = LogManager.getLogger();
    
    private static final String JSON_PATH = "/training.json";
    private final ConcurrentMap<String, String> canonicalQuestions = new ConcurrentHashMap<>();
    
    public TrainingJsonCanonicalQuestionService() {
        loadJSON(JSON_PATH);
    }

    @Override
    public String lookup(String className) {
        logger.entry(className);
        return logger.exit(canonicalQuestions.get(className));
    }
    
    private void loadJSON(String path) {
        try(InputStream is = getClass().getResourceAsStream(path)) {
            if(is == null) {
                throw new IOException(MessageKey.AQWQAC14101E_could_not_find_path_on_classpath_1.getMessage(path).getFormattedMessage());
            }
            
            NLClassifierTrainingData data = NLClassifierTrainingData.fromStream(is);
            for(TrainingInstance instance : data.getTrainingData()) {
                List<String> classes = instance.getClasses();
                canonicalQuestions.putIfAbsent(classes.get(0), instance.getText());
            }
        } catch (IOException e) {
            logger.catching(e);
        }
    }
}
