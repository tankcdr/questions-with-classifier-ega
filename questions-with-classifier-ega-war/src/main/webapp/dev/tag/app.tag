/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<app>
	<welcome if={showWelcome} onclose={hideWelcomeScreen}></welcome>
	<home if={showHome} class="home"></home>
	<service-unavailable if={showUnavailable} onclose={hideErrorScreen}></service-unavailable>
	<menu-overlay if={showMenu} class="mobileMenuHidden" onclose={hideMenuOverlay}></menu-overlay>
    
    <script>
    
    var action        = require("./action.js"),
        routingAction = require("./routingAction.js"),
        constants     = require("./constants.js"),
        riot          = require("riot"),
        self          = this;
    
    self.showWelcome         = false;
    self.showHome            = false;
    self.showUnavailable     = false;
    self.showMenu            = false;
    
    // Routing control variables
    self.isValidConversation = false;
    self.isValidMessage      = false;
    
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
    
    //----------------------------------------------------------------------------
    // Routing
    //----------------------------------------------------------------------------
    
    /*
     * Guarantees the requestedFeedback is a proper type
     * @param {String} Feedback string
     * @returns {Boolean} True if this is a type of feedback we can give the server, false otherwise
     */
    _isProperFeedback(requestedFeedback) {
        
        // Only return accepted feedback types, and make sure we don't execute the "still need help" call, since it's a UI
        // update and not a new question for the server
        return (requestedFeedback !== constants.needHelpFeedbackType) &&
               ((requestedFeedback === constants.refinementQueryType) || (requestedFeedback === constants.topQuestionFeedbackType)) ||
               (!requestedFeedback);
    }
    
    Dispatcher.on(action.CONVERSATION_STARTED_BROADCAST, function(conversation) {
        Dispatcher.trigger(routingAction.CONVERSATION_STARTED, conversation.conversationId);
    });
    
    // Establish our main routing callback to handle the url resolution

    riot.route(function(requestedConversationId, requestedMessageText, requestedFeedback) {
        
        // Load the home page if we're only getting a conversationId
        if (requestedConversationId && !requestedMessageText && !requestedFeedback) {
            Dispatcher.trigger(routingAction.SHOW_HOME_PAGE);
        }
        
        // If it's a refinement, we'll handle that elsewhere since we're not showing a simple answer
        if (requestedMessageText && self._isProperFeedback(requestedFeedback)) {
            
            // Don't send a referrer object if we have none
            var messagePayload     = {};
            messagePayload.message = decodeURIComponent(requestedMessageText);
            
            if (requestedFeedback) {
                messagePayload.referrer = requestedFeedback;
            }
            
            // Fire off our question
            Dispatcher.trigger(action.ASK_QUESTION, messagePayload);
        }
    });
    
    
    // Initiate a conversation when the app is mounted
    self.on("mount", function() {
        Dispatcher.trigger(action.CONVERSATION_START);
        Dispatcher.trigger(action.GET_VISIT_LEVEL);
    });
    
    </script>
</app>