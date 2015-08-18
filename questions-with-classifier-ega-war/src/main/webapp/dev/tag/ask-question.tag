/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<ask-question>
    <answer if={this.showAnswer} name="answerTag" class="answer ask-question-inner-container noAnswer"></answer>
    <user-unhappy-container if={this.showUnhappyContainer} class="ask-question-inner-container"></user-unhappy-container>
    <question-input name="questionInputTag" class="question-input-container noAnswer" initialviewing=true></question-input>
    
    <script>
    var action        = require("./action.js"),
        routingAction = require("./routingAction.js"),
        constants     = require("./constants.js"),
        self          = this;
    
    // Initially, don't show answers to questions we haven't asked yet
    self.on("mount", function() {
        self.showAnswer           = true;
        self.showUnhappyContainer = false;
        self.update();
    });

    // Handle the routing if we're showing a refinement
    riot.route(function(requestedConversationId, requestedMessageText, requestedFeedback) {
        
        // If it's a refinement, we'll handle that elsewhere since we're not showing a simple answer
        if (requestedConversationId && requestedFeedback === constants.needHelpFeedbackType) {
            self.showAnswer           = false;
            self.showUnhappyContainer = true;
            self.update();
        }
    });
    
    Dispatcher.on(routingAction.SHOW_HOME_PAGE_BROADCAST, function() {
        self.showAnswer           = true;
        self.showUnhappyContainer = false;
        self.root.classList.add("initialViewing");
        self.update();
    });
    
    // When we've received negative feedback
    Dispatcher.on(action.NEGATIVE_FEEDBACK_RECEIVED_BROADCAST, function() {
        
        // Do an in-place swap of the URL
        riot.route.exec(function(requestedConversationId, requestedMessageText, requestedFeedback) {
            
            // Tack on REFINEMENT to the feedback section of the url
            Dispatcher.trigger(routingAction.REFINEMENT_REQUESTED, 
                { conversationId : requestedConversationId, message : requestedMessageText, referrer : constants.needHelpFeedbackType }
            );
        });
        
        self.showAnswer           = false;
        self.showUnhappyContainer = true;
        self.update();
    });
    
    // When a question has been asked
    Dispatcher.on(action.ASKING_QUESTION_BROADCAST, function(question) {
        self.root.classList.add("blurred");
        self.opts.showindicator();
    });
    
    // After an answer has been received for a question
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        
        self.showAnswer           = true;
        self.showUnhappyContainer = false;
        self.root.classList.remove("blurred");
        self.root.classList.remove("initialViewing");
        self.opts.hideindicator();
        self.update();
    });
    
    Dispatcher.on(action.SERVER_ERROR_BROADCAST, function() {
        self.root.classList.remove("blurred");
        self.opts.hideindicator();
    });
    
    </script>
</ask-question>
