/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<alternative-questions>
    <div if={this.showQuestion} class="questionContainer">
        <h3 id="questionText" class="question-text"></h3>
    </div>
    
    <div if={this.showSorry} class="refinement-title">{sorryTitle}</div>
    
    <ul class="questionList" if={answersToShow.length}>
    	<li each={ answer, i in answersToShow } class="question refinement-white" onclick={ parent.askQuestion } id="dym-question-{ i }">
    		<span class="low-confidence-answer-text">{answer.canonicalQuestion}</span>
    	</li>
    	<li if={this.isAnswerView} class="none" onclick={noneOfTheAbove}>
    		<span id="noneOfAboveLabel" class="low-confidence-answer-text"></span>
    	</li>
    </ul>
    <still-need-help if={showStillNeedHelp} class="stillNeedHelp"></still-need-help>
    
    <script>
    
    var action        = require("./action.js"),
        routingAction = require("./routingAction.js"),
        constants     = require("./constants.js"),
        self          = this;
    
    self.showQuestion   = opts.showquestion === "true" ? true : false;
    self.showSorry      = opts.showsorry    === "true" ? true : false;
    self.isAnswerView   = opts.isanswerview === "true" ? true : false;
    self.sorryTitle     = polyglot.t("refineMyQuestionSubtitle");
    self.noneOfAboveLabel.innerHTML = polyglot.t("noneOfAbove");
        
    // When this is mounted, ask for alternative questions
    self.on("mount", function() {
        Dispatcher.trigger(action.GET_ALTERNATIVE_QUESTIONS);
        self.showStillNeedHelp = false;
    });
    
    // Since we can't scroll before an update happens, we have to scroll down after
    self.on("update", function() {
        if (self.showStillNeedHelp) {
            window.scrollTo(0, document.body.scrollHeight);
        }
    });

    askQuestion(e) {
        Dispatcher.trigger(routingAction.ASK_QUESTION, 
            {"message" : e.item.answer.canonicalQuestion, "referrer" : constants.refinementQueryType});
    }
    
    noneOfTheAbove(e) {
    	self.showStillNeedHelp = true;
        self.noneOfAboveLabel.innerHTML = "<img src=\"images/Badge_88.svg\" class=\"yayIcon\" />" + polyglot.t("thanksForFeedbackMessage");
        self.noneOfAboveLabel.classList.add("selected");
        
        self.update();
    }
    
    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
        
        var responses = conversation.responses;
        
        var hasAnswers              = responses.length > 0;
        var highestConfidenceAnswer = hasAnswers              && responses[0];
        var showLowConfidence       = highestConfidenceAnswer && highestConfidenceAnswer.confidenceCategory === "LOW";
        
        if (showLowConfidence) {
            self.showStillNeedHelp = false;
            self.noneOfAboveLabel.innerHTML = polyglot.t("noneOfAbove");
            self.noneOfAboveLabel.classList.remove("selected");
            self.update();
        }
    });
    
    Dispatcher.on(action.ALTERNATIVE_QUESTION_BROADCAST, function(incomingData) {
        self.responses              = incomingData.responses;
        self.questionText.innerHTML = "<b>\"" + incomingData.message + "\"</b>";
        
        // If there aren't any responses to show, make sure not to crash
        if (self.responses) {
            if (self.isAnswerView) {
                self.answersToShow = self.responses;
            }
            else {
            // Start with the second answer if we're showing a "I still need help" list of questions
                self.answersToShow = self.responses.slice(1);
            }
    
            self.update();
        }
    });
    
    </script>
</alternative-questions>