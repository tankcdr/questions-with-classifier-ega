/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<question-input>
    <h3 if={evalStringToTrue(this.initialviewing)} class="getting-started-desktop"><b>{subtitleText}</b></h3>
    <div class={stickInputBoxToBottomWhenNotFirstTime()}>
        <form name="questionInputForm" class="form-inline" onsubmit={questionAsked}>
            
            <div class="form-group">
                <!-- Input box -->
                <input type="text" 
                       class="form-control input-lg question-input-box" 
                       id="questionInputField"
                       placeholder={questionPlaceHolderText}/>
                
                <!-- Popups -->
                <div id="questionValidationError" class="popup-block hidden">
                    <a id="close" href="#" onclick={closeLinkPressed}>
                        <img id="validationClose" src="/images/close-icon.svg" />
                    </a>
                    <p><span id="questionValidationMessage"></span></p>
                </div>
            </div>

            <button onclick={askButtonPressed} 
                    id="askButton"
                    type="submit" 
                    class="btn btn-primary input-lg square">{askButtonLabel}</button>
        </form>
    </div>
    
    <script>
    
    var self          = this,
        action        = require("./action.js"),
        routingAction = require("./routingAction.js");
        
	self.initialviewing              = opts.initialviewing;
	self.questionPlaceHolderText     = polyglot.t("typicalQuestionText");
	self.askButtonLabel              = polyglot.t("askButton");
    self.subtitleText                = polyglot.t("subTitle");
    self.askButton.disabled          = true;
    self.questionInputField.disabled = true; 

    addPlaceholderText() {
        self.questionInputField.placeholder = polyglot.t("typicalQuestionText");
    }
    
    stickInputBoxToBottomWhenNotFirstTime() {
        return self.evalStringToTrue(self.initialviewing) ? "ask-question-container" : "ask-question-container bottom";
    }
    
    // Not sure why, but Riot can't inline evaluate string comparisons
    evalStringToTrue(inputString) {
        return inputString === "true" ? true : false;
    }
    
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        self.root.classList.remove("active");

        self.questionInputField.value = "";
        self.questionInputField.blur();
        self.initialviewing = "false";
        
        self.addPlaceholderText();
        self.root.classList.remove("noAnswer");

        self.update();
    });
    
    Dispatcher.on(action.CONVERSATION_STARTED_BROADCAST, function(conversation) {
        self.askButton.disabled          = false;
        self.questionInputField.disabled = false; 
    });
    
    validateQuestion(question) {
        // Basic input validation
        if (!question.trim()) {
            self.badQuestion(polyglot.t("validationError-oneword"));
            return false;
        }
        // Check for limitations of the classifier service
        if (question.length > 1024) {
            self.badQuestion(polyglot.t("validationError-maxQueryLength"));
            return false;
        }
        words = question.split(" ");
        for (var i=0; i<words.length; i++) {
            if (words[i].length > 46) {
                self.badQuestion(polyglot.t("validationError-maxWordLength"));
                return false;
            }
        }
        return true;
    }
    
    badQuestion(errorMessage) {
        self.questionValidationMessage.innerHTML = errorMessage;
        if (document.getElementsByClassName("ask-question-container bottom").length > 0) {
            self.questionValidationError.className = "popup-block";
        } else {
            self.questionValidationError.className = "popup-block-below";
        }
        
        self.questionValidationError.classList.remove("hidden");
        self.questionValidationError.classList.add("active");
    }
    
    // Event handler for the question validation error
    closeLinkPressed(e) {
        self.questionValidationError.classList.add("hidden");
        self.questionValidationError.classList.remove("active");
    }
    
    // Event handler for the button
    askButtonPressed(e) {
        var questionText = self.questionInputField.value;
        
        // Perform validation and fire off our event if this text is valid
        if (questionText && self.validateQuestion(questionText)) {
            self.questionValidationError.classList.remove("active");
            Dispatcher.trigger(routingAction.ASK_QUESTION, { message : questionText });
        }
    }

    </script>
</question-input>
