/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<high-confidence-answer>
    <div class="questionContainer">
        <h3 id="questionText" class="question-text"></h3>
    </div>
    <div class="answer-quote-container">
        <div id="answerText" class="answer-quote"></div>
    </div>
    <!-- Feedback Buttons -->
    <div if={this.showFeedback} class="ask-question-container mobileFeedbacks">
        <user-happy-container if={showThankYou}></user-happy-container>
        <positive-feedback-input if={!showThankYou} id="positiveFeedbackInput" class="left-feedback-container"></positive-feedback-input>
        <negative-feedback-input if={!showThankYou} id="negativeFeedbackInput" class="right-feedback-container"></negative-feedback-input>
        
        <div if={showPopup} id="popup" class="popup-block-below hidden">
            <a id="close" href="#" onclick={closeLinkPressed}> 
                <img id="popupClose" src="/images/close-icon.svg" />
            </a>
            <p>{popupText}</p>
        </div>
    </div>
    
    <script>
    var self  = this,
    action    = require("./action.js"),
    constants = require("./constants.js");
    
    self.popupCount   = 0;
    self.showPopup    = false;
    self.showFeedback = false;
    self.showThankYou = false;
    self.popupText    = polyglot.t("popUp");
    
    self.on("update", function() {
        
        // THIS shows the animation
        if (self.showWelcomePopup) {
            
            // Remove the active class in order to force the animation again
            self.popup.classList.remove("active");
            
            // Wasn't that fun?
            window.setTimeout(function() {
                self.popup.classList.add("active");
            }, 1000);
        }
    });
    
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        self.showFeedback           = true;
        self.questionText.innerHTML = "<b>\"" + conversation.message + "\"</b>";
        self.answerText.innerHTML   = conversation.responses && conversation.responses[0] && conversation.responses[0].text;
        
        // This doesn't actually show the popup, but if the visit level allows us to, then it will.
        // Hacking around the issue where mount is run WAY before this answer is shown, and we lose
        // the animated effect.
        if (self.popupCount < constants.showPopupCount) {
            self.showPopup = true;
            ++self.popupCount;
            
            // Check whether we're going to save the state afte incrementing and never show popup again
            if (self.popupCount >= constants.showPopupCount) {
                Dispatcher.trigger(action.SET_VISIT_LEVEL, constants.visitLevels.POPUP);
            }
            
        }
        else {
            self.showPopup = false;
            self.showWelcomePopup = false;
        }
        
        self.showThankYou = false;
        Dispatcher.trigger(action.GET_VISIT_LEVEL);

        self.update();
    });
    
    Dispatcher.on(action.VISIT_LEVEL_WELCOME_BROADCAST, function(visitLevel) {
        if (self.showPopup) {
            // This actually shows the popup, but not right now as the page hasn't rendered yet
            //....  thus losing the animation
            self.showWelcomePopup = true;
            self.popup.classList.remove("hidden");
            
            self.update();
        }
    });
    
    Dispatcher.on(action.POSITIVE_FEEDBACK_RECEIVED_BROADCAST, function() {
        self.showThankYou     = true;
        self.showWelcomePopup = false;
        self.showPopup        = false;
        self.update();
    });
    
    // Event handler for the question validation error
    closeLinkPressed(e) {
        self.showPopup        = false;
        self.showWelcomePopup = false;
        self.popup.classList.add("hidden");
        self.popup.classList.remove("active");
        self.popupCount = constants.showPopupCount;
        Dispatcher.trigger(action.SET_VISIT_LEVEL, constants.visitLevels.POPUP);
        self.update();
    }

    </script>
</high-confidence-answer>
