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

package com.ibm.watson.app.qaclassifier.services.answer;

import com.google.inject.Inject;
import com.ibm.watson.app.common.services.box.BoxService;
import com.ibm.watson.app.common.services.box.model.BoxMetadataResponse;
import com.ibm.watson.app.common.services.box.model.BoxSearchResponse;
import com.ibm.watson.app.common.services.box.model.BoxSearchResponse.FileInfo;
import com.ibm.watson.app.common.services.nlclassifier.model.NLClassiferClassifyResponse.NLClassifiedClass;
import com.ibm.watson.app.qaclassifier.rest.model.Answer;
import com.ibm.watson.app.qaclassifier.rest.model.Answer.ConfidenceCategoryEnum;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

public class BoxAnswerResolver implements AnswerResolver {
	private static final String TEMPLATE = "properties";
	private static final String SCOPE = "global";
	
	private final BoxService boxService;

	@Inject
	public BoxAnswerResolver(BoxService service) {
		this.boxService = service;
	}
	
	@Override
	public Answer resolve(NLClassifiedClass classifiedClass) throws ResolutionException {
        final String className = classifiedClass.getClassName();
        final Double confidence = classifiedClass.getConfidence();
        final ConfidenceCategoryEnum confidenceCategory = null; // This is now done on the entire answer list, after answer resolution
        
        // there are going to have to be 3 calls to resolve the answer (search, metadata, content)
        String answerText = "";
        String canonicalQuestion = "";
        
		BoxSearchResponse searchResponse = boxService.search(className);

		// get the metadata
		String fileId = null;
		if( searchResponse != null && searchResponse.getEntries() != null ) {
			for( FileInfo f : searchResponse.getEntries() ) {
				// assumption is that the files are named className.html
				if( f.getName().equals(className+".html") ) {
					fileId = f.getId();
					break;
				}
			}
		}

        if(fileId == null) {
        	// TODO - replace with a new box specific message string
        	throw new ResolutionException(MessageKey.AQWQAC24150E_could_not_find_key_in_db_1.getMessage(className).getFormattedMessage());
        }

        // grab the canonical question from the metadata
	    BoxMetadataResponse metadataResponse = boxService.getMetadata(fileId);
	    for( BoxMetadataResponse.Entry e : metadataResponse.getEntries() ) {
	    	if( e.getScope().equals(SCOPE) && e.getTemplate().equals(TEMPLATE)) {
	    		canonicalQuestion = e.getCanonicalQuestion();
	    	}
	    }
	    
	    if( canonicalQuestion == null || canonicalQuestion.isEmpty() ) {
	    	// TODO - need message key
	    	throw new ResolutionException("Unable to retrieve canonical question for class");
	    }
	    
	    // retrieve the content of the file
	    answerText = boxService.getFileContents(fileId);
	    
	    if( answerText == null || answerText.isEmpty() ) {
	        // TODO - put a message key here
	    	throw new ResolutionException("Unable to retrieve file contents for class");
	    }
	    
        return new Answer(Answer.TypeEnum.TEXT, answerText, confidence, confidenceCategory, canonicalQuestion, className);
	}

}
