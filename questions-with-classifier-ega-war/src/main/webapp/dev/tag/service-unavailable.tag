/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<service-unavailable>

    <div class="screen-container">
        <div class="logo-container">
            <loading-indicator state="welcome"></loading-indicator>
            <div id="error">{error.message}</div>
        </div>
        <div class="text-container">
            <div class="text-inner-container">
                <div class="horizontal-line">.</div>
                <div id="screenText">{wittyText}</div>
                <br>
                <div id="screenTextSubtitle">{serviceUnavailableSub}</div>
            </div>
            
            <footer class="footer">
                <a id="actionButton" href="#" onclick={retryPressed}>{serviceUnavailableButton}</a>
            </footer>
        </div>
    </div>

	<script>
    
    var self   = this,
        action = require("./action.js");
        
    self.serviceUnavailableSub    = polyglot.t("service-unavailable-sub");
    self.serviceUnavailableButton = polyglot.t("service-unavailable-button");
	self.wittyResponses           = [
		polyglot.t("service-unavailable-0"),
		polyglot.t("service-unavailable-1")
	];
    
    Dispatcher.on(action.SERVER_ERROR_BROADCAST, function(error) {
        self.error.message = error;
        self.wittyText     = self.wittyResponses[Math.floor(Math.random() * self.wittyResponses.length)];
        self.update();
    });
    
	retryPressed(e) {
		self.opts.onclose && self.opts.onclose.call();
	};
    
	</script>
</service-unavailable>
