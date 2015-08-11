/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<user-happy-container>
    <div id="statusBar" class="statusBar">
        <img src="images/Badge_88.svg" class="thanksImage" />
        <span>
            <strong>{this.funMessages[this.currentMessage].title}</strong>
            <span>{this.funMessages[this.currentMessage].subtitle}</span>
        </span>
    </div>
    
    <script>
    var self   = this,
        action = require("./action.js");
    
    self.on("mount", function() {
        self.currentMessage = 0;
        self.update();
    });
    
    self.funMessages = [{"title" : polyglot.t("posFeedbackTitle-1"), "subtitle" : polyglot.t("posFeedbackSubTitle-1")},
                        {"title" : polyglot.t("posFeedbackTitle-2"), "subtitle" : polyglot.t("posFeedbackSubTitle-2")},
                        {"title" : polyglot.t("posFeedbackTitle-3"), "subtitle" : polyglot.t("posFeedbackSubTitle-3")},
                        {"title" : polyglot.t("posFeedbackTitle-4"), "subtitle" : polyglot.t("posFeedbackSubTitle-4")}];
                        
    nextFunMessage() {
        self.currentMessage = (self.currentMessage + 1) % self.funMessages.length;
    };
    
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        self.nextFunMessage();
    });
    
    </script>
</user-happy-container>