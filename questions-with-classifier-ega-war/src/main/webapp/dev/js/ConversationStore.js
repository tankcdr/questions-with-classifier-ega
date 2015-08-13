/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var riot          = require("riot"),
    action        = require("./action.js"),
    routingAction = require("./routingAction.js"),
    constants     = require("./constants.js");

// ConversationStore definition.
// Flux stores house application logic and state that relate to a specific domain.
// In this case, a list of todo items.
function ConversationStore() {
    riot.observable(this);

    var self = this;

    self.conversation = {
        conversationId : "",
        message        : "",
        messageId      : "",
        responses      : [],
        topQuestions   : []
    };

    /**
     * Takes a response and processes its body for a JSON response
     * @param {Object} Server response with a body in JSON format
     * @return {Promise} A Promise that, when resolved, returns an Object of responses from the server
     */
    function _parseJson(response) {
        return response.json();
    }
    
    /**
     * Takes a response and processes its response status type
     * @param {Object} Server response
     * @return {Object} The original server response if status is GOOD
     * @throws {Error} Response status is not GOOD
     */
    function _analyzeStatus(response) {
    
        if (response.status === 200) {
            return response;
        }

        throw new Error(response.statusText);
    }

	/**
	 * Initiates a conversation with the server and triggers a response event
	 */
    self.on(action.CONVERSATION_START, function() {
        
        fetch(constants.conversationUrl, {
            method: "post",
            headers: { "Content-Type": "application/json" }
        })
        .then(function(response) {
            if (response.status === 200) {
                return response;
            }
            throw new Error(response.statusText);
        })
        .then(function(response) {
           return response.json();
        })
        .then(function(data) {
            self.conversation.conversationId = data.conversationId;
            self.conversation.topQuestions   = data.topQuestions;
            
            self.trigger(action.CONVERSATION_STARTED_BROADCAST, self.conversation);
            self.trigger(action.GET_CONVERSATION_ID_BROADCAST, self.conversation.conversationId);
            self.trigger(action.TOP_QUESTIONS_BROADCAST, self.conversation.topQuestions);
        })
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });
    
    /**
     * Responds to a query for the current conversation ID, 
     * and triggers a broadcast of the ID if it exists
     */
    
    self.on(action.GET_CONVERSATION_ID, function() {
        if (!self.conversation || !self.conversation.conversationId) {
            self.trigger(action.CONVERSATION_START);
        }
        else {
            self.trigger(action.GET_CONVERSATION_ID_BROADCAST, self.conversation.conversationId);
        }
    });
    
	/**
	 * Asks a question to the server, updates the local store, triggers a response event
	 */
    self.on(action.ASK_QUESTION, function(question) {
        
        self.trigger(action.ASKING_QUESTION_BROADCAST);
        
        var requestBody = {};
        
        if (question.message) {
            requestBody.message = question.message;
        }

        if (question.referrer) {
            requestBody.referrer        = {};
            requestBody.referrer.source = question.referrer;
            if (question.referrer === constants.refinementQueryType) {
                requestBody.referrer.messageId = self.conversation.messageId;
            }
        }
        
        fetch(constants.conversationUrl + self.conversation.conversationId, {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestBody)
        })
        .then(_analyzeStatus)
        .then(_parseJson)
        .then(function(data) {
            
            self.conversation.responses = data.responses;
            self.conversation.messageId = data.messageId;
            self.conversation.message   = data.message;

            self.trigger(action.ANSWER_RECEIVED_BROADCAST, self.conversation);
            self.trigger(action.ALTERNATIVE_QUESTION_BROADCAST, self.conversation);
        })
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });
    
	/**
	 * None of the above was clicked, so let's log that feedback
	 */
    self.on(action.NONE_OF_THE_ABOVE_CLICKED, function(messageData){
        // The call to the server is for feedback only, so we don't need 
        // to wait for it to trigger the broadcast action
        self.trigger(action.NONE_OF_THE_ABOVE_CLICKED_BROADCAST);
        
        var postData = {
            "conversationId": self.conversation.conversationId,
            "messageId": self.conversation.messageId,
            "action": "NO_HELPFUL_REFINEMENTS" 
        };

        fetch(constants.feedbackUrl, {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData)
        })
        .then(function(response) {
            if (response.status < 200 || response.status >= 300) {
                throw new Error(response.statusText);
            }
        }.bind(this))
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });

    /**
     * The user clicked on something to provide negative feedback
     */
    self.on(action.NEGATIVE_FEEDBACK_GIVEN, function() {
        var postData = {
            "conversationId": self.conversation.conversationId,
            "messageId": self.conversation.messageId,
            "action": "UNHELPFUL" 
        };

        fetch(constants.feedbackUrl, {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData)
        })
        .then(function(response) {
            if (response.status < 200 || response.status >= 300) {
                throw new Error(response.statusText);
            }
            
            self.trigger(action.NEGATIVE_FEEDBACK_RECEIVED_BROADCAST);
        })
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });

    /**
     * The user clicked on something to provide positive feedback
     */
    self.on(action.POSITIVE_FEEDBACK_GIVEN, function() {
        var postData = {
            "conversationId": self.conversation.conversationId,
            "messageId": self.conversation.messageId,
            "action": "HELPFUL" 
        };

        fetch(constants.feedbackUrl, {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData)
        })
        .then(function(response) {
            if (response.status < 200 || response.status >= 300) {
                throw new Error(response.statusText);
            }
            
            self.trigger(action.POSITIVE_FEEDBACK_RECEIVED_BROADCAST);
        })
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });
    
    /**
     * The user clicked on something to open up the forum in a new tab
     */
    self.on(action.FORUM_BUTTON_PRESSED, function() {
        var postData = {
                "conversationId": self.conversation.conversationId,
                "messageId": self.conversation.messageId,
                "action": "FORUM_REDIRECT" 
        };
        
        fetch(constants.feedbackUrl, {
            method: "post",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData)
        })
        .then(function(response) {
            if (response.status < 200 || response.status >= 300) {
                throw new Error(response.statusText);
            }
        })
        .catch(function(error) {
            self.trigger(action.SERVER_ERROR_BROADCAST, error);
        });
    });

	/**
	 * Checks the local cache for a current list of alternative questions, and triggers a broadcast
     * with them in the payload.
	 */
    self.on(action.GET_ALTERNATIVE_QUESTIONS, function() {
        
        self.trigger(action.ALTERNATIVE_QUESTION_BROADCAST, self.conversation);
    });
    
	/**
	 * Checks the local cache for a current list of top questions, and triggers a broadcast
     * with them in the payload.
	 */
    self.on(action.GET_TOP_QUESTIONS, function() {
        
        if (self.conversation.conversationId && !self.conversation.topQuestions) {

            fetch(constants.conversationUrl + self.conversation.conversationId + "/topQuestions", {
                method: "get",
                headers: { "Content-Type": "application/json" }
            })
            .then(_analyzeStatus)
            .then(_parseJson)
            .then(function(data) {
            
                self.conversation.topQuestions = data.topQuestions;

                self.trigger(action.TOP_QUESTIONS_BROADCAST, self.conversation.topQuestions);
            })
            .catch(function(error) {
                self.trigger(action.SERVER_ERROR_BROADCAST, error);
            });
        }
        else if (self.conversation && self.topQuestions) {
            self.trigger(action.TOP_QUESTIONS_BROADCAST, self.conversation.topQuestions);
        }
    });
}

if (typeof(module) !== 'undefined') module.exports = ConversationStore;