/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<question-unanswerable>
    <h3 if={lowConfidence} id="questionTitle" class="blueTitle"></h3>
    <div if={lowConfidence} class="refinement-title">{sorryTitle}</div>
	<still-need-help class="stillNeedHelp"></still-need-help>	
	<top-questions-tab if={ this.showTopQuestions }></top-questions-tab>
	
	<script>
    
    var self   = this,
        action = require("./action.js");
    
    self.on("update", function() {
    	self.showTopQuestions = false;
        self.lowConfidence    = opts.lowconfidence === "true" ? true : false;
        self.sorryTitle       = polyglot.t("lowConfidenceSubtitle");
    });

    Dispatcher.on(action.ANSWER_RECEIVED_BROADCAST, function(conversation) {
       self.questionTitle.innerHTML = "\"<strong>" + conversation.message + "</strong>\"";
    });
    
	</script>
</question-unanswerable>
