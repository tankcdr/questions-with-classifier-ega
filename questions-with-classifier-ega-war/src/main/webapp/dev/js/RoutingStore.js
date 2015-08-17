/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var riot          = require("riot"),
    action        = require("./action.js"),
    routingAction = require("./routingAction.js");
    
function RoutingStore() {
    riot.observable(this);
    
    var self = this;

    self.currentConversationId = null;
    
    /*
     *  Responds to an action notifying this store that a conversation has been started
     * @param {String} The ID of the current conversation we need to route to 
     */ 
    self.on(routingAction.CONVERSATION_STARTED, function(conversationId) {
        self.currentConversationId = conversationId;
        riot.route(conversationId);
    });
    
    /*
     *  Responds to an action notifying this store that a conversation has been started
     * @param {Object} An object with: message, (optional)referrer
     */ 
    self.on(routingAction.ASK_QUESTION, function(questionPayload) {
        var routeTo = self.currentConversationId + "/" +
                      encodeURIComponent(questionPayload.message) + "/" +
                      (questionPayload.referrer ? questionPayload.referrer : "");

        riot.route(routeTo);
    });
}

if (typeof(module) !== 'undefined') module.exports = RoutingStore;