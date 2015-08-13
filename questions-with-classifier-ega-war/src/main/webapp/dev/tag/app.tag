/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<app>
	<welcome if={showWelcome} onclose={hideWelcomeScreen}></welcome>
	<home if={showHome} class="home"></home>
	<service-unavailable if={showUnavailable} onclose={hideErrorScreen}></service-unavailable>
	<menu-overlay if={showMenu} class="mobileMenuHidden" onclose={hideMenuOverlay}></menu-overlay>
    
    <script>
    
    var action        = require("./action.js"),
        routingAction = require("./action.js"),
        constants     = require("./constants.js"),
        riot          = require("riot"),
        self          = this;
    
    self.showWelcome     = false;
    self.showHome        = false;
    self.showUnavailable = false;
    self.showMenu        = false;
    
    hideWelcomeScreen = function() {
        self.showWelcome = false;
        self.showHome    = true;
        self.update();
    };
    
    hideErrorScreen = function() {
        self.showUnavailable = false;
        self.showHome        = true;
        self.update();
    };
    
    hideMenuOverlay = function() {
        self.showMenu = false;
        self.update(); 
    };
    
    Dispatcher.on(action.VISIT_LEVEL_BROADCAST, function(visitLevel) {
        if (visitLevel === constants.visitLevels.NONE) {
            self.showWelcome = true;
        }
        else {
            self.showHome = true;
        }
        self.update();
    });
    
    Dispatcher.on(action.SHOW_MENU_OVERLAY_BROADCAST, function(data) {
        self.showMenu = true;
        self.tags["menu-overlay"].content = data.content;
        self.tags["menu-overlay"].title   = data.title;
        self.update();
        self.tags["menu-overlay"].menuOverlay.focus();
    });
    
    Dispatcher.on(action.SERVER_ERROR_BROADCAST, function(error) {
        self.showUnavailable = true;
        self.showWelcome     = false;
        self.showHome        = false;
        self.update();
    });
    
    
    // Routing
    
    // Establish our main routing callback to handle changes to the hash
    riot.route(function(requestedConversation, requestedMessage, requestedFeedback) {
        
        // Async handler functions
        //
        // Handle a conversation start by the server
        Dispatcher.on(action.CONVERSATION_STARTED_BROADCAST, function(storedConversation) {
            Dispatcher.trigger(routingAction.CONVERSATION_STARTED, storedConversation);
        });
        // Make sure the current conversation is the one requested in the URL
        Dispatcher.on(action.GET_CONVERSATION_ID_BROADCAST, function(conversationId) {
            if (conversationId !== conversationId) {
                Dispatcher.trigger(action.CONVERSATION_START);
            }
        });
        
        // Make sure we have a started conversation and a current conversation
        if (!requestedConversation) {
            Dispatcher.trigger(action.CONVERSATION_START);
        }
        else {
            Dispatcher.trigger(action.GET_CONVERSATION_ID);
        }
    });
    
    
    // Initiate a conversation when the app is mounted
    self.on("mount", function() {
        Dispatcher.trigger(action.CONVERSATION_START);
        Dispatcher.trigger(action.GET_VISIT_LEVEL);
    });
    
    </script>
</app>