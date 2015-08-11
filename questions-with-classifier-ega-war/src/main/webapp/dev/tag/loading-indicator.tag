/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<loading-indicator>
	<div class="loader" id="loaderCall">
		<object type="image/svg+xml" data="images/loading-indicator-thinking.svg" id="loaderImage" class="loader--svg"> </object>
		<object type="image/svg+xml" data="images/loading-indicator.svg" id="staticImage" class="loader--svg"> </object>
		<div class="loader--fallback" if={showing} id="fallback"></div>
	</div>
	<script>
		var action = require("./action.js"),
			self   = this,
			animationController = false; 

		self.on("mount", function() {
			if(self.opts.setindicator) {
				var indicatorCalls = {"showIndicator" : self.showIndicatorFunc, "hideIndicator" : self.hideIndicatorFunc};
				self.opts.setindicator(indicatorCalls);
			}
			if(self.opts.state == "welcome") {
				self.showing = true;
			}
			
			self.update();
		});

		showIndicatorFunc(){
			////SVG Support////
			if(self.loaderImage.contentDocument && !document.documentElement.classList.contains("no-smil")) { //IE Fails if goes into if statement
				self.loaderImage.classList.add("active");
				if(animationController)
					self.loaderImage.contentDocument.querySelector("#ani-exterior-stroke").beginElement();
				self.loaderImage.contentDocument.querySelector("#svg-all").classList.remove("hidden");
				self.loaderImage.contentDocument.querySelector("#svg-all").classList.remove("scale-down");
			}
			//Fallback for gif
			self.showing = true;
			self.update();
		};

		hideIndicatorFunc() {
			////SVG Support////
			if(self.loaderImage.contentDocument && !document.documentElement.classList.contains("no-smil")) {
				self.loaderImage.classList.remove("active");
				self.loaderImage.contentDocument.querySelector("#ani-exterior-stroke-complete").beginElement();
				self.loaderImage.contentDocument.querySelector("#svg-all").classList.add("scale-down");
				animationController = true;
			}
			//Fallback for gif
			self.showing = false;
			self.update();
		};

	</script>
</loading-indicator>
