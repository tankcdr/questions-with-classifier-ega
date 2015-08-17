/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<home-tab-container>
    <top-questions-tab class="top-questions-tab"></top-questions-tab>
    <powered-by class="poweredBy-home"></powered-by>
    
    <script>
    var action = require("./action.js"),
        self   = this;

    // When a question has been asked
    Dispatcher.on(action.ASKING_QUESTION_BROADCAST, function(question) {
        self.root.classList.add("blurred");
    });
    
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        self.root.classList.add("questionHasBeenAsked");
        self.root.classList.remove("blurred");
    });
    
    Dispatcher.on(action.SERVER_ERROR_BROADCAST, function() {
        //////////Mobile blur////////////////
        self.root.classList.remove("questionHasBeenAsked");
        self.root.classList.remove("blurred");
    });
    </script>
</home-tab-container>
