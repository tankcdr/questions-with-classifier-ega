/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<ask-question>
    <answer if={this.showAnswer} name="answerTag" class="answer ask-question-inner-container noAnswer"></answer>
    <user-unhappy-container if={this.showUnhappyContainer} class="ask-question-inner-container"></user-unhappy-container>
    <question-input name="questionInputTag" class="question-input-container noAnswer" initialviewing=true></question-input>
    
    <script>
    var action    = require("./action.js"),
        self      = this;
    
    // Initially, don't show answers to questions we haven't asked yet
    self.on("mount", function() {
        self.showAnswer           = true;
        self.showUnhappyContainer = false;
        self.update();
    });

    // When we've received negative feedback
    Dispatcher.on(action.NEGATIVE_FEEDBACK_RECEIVED_BROADCAST, function() {
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
        self.showHappyContainer   = false;
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
