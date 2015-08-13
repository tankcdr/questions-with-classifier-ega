/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var riot          = require("riot"),
    routingAction = require("./routingAction.js");
    
function RoutingStore() {
    riot.observable(this);
    
    var self = this;
    
    self.on(routingAction.CONVERSATION_STARTED, function(conversation) {
        riot.route(conversation.conversationId);
    });
}

if (typeof(module) !== 'undefined') module.exports = RoutingStore;