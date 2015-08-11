/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<negative-feedback-input>
    <button onclick={negativeFeedbackButtonPressed} 
        id="askButton" 
        type="submit" 
        class="btn-feedback">
            <img src="images/Wrong_88.svg" class="feedback-img">
            <p> {posFeedbackText} </p>
    </button>

    <script>
    var action = require("./action.js");
    this.posFeedbackText = polyglot.t("negFeedbackButton");
    
    negativeFeedbackButtonPressed(e) {
        Dispatcher.trigger(action.NEGATIVE_FEEDBACK_GIVEN);
    }
    
    </script>
</negative-feedback-input>
