/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<menu-overlay>
	<div id="menuOverlay" tabindex="0" onkeyup={closeMenuOnESC} onclick={closeMenuOnOverlayClick}>
		<div id="menu">
		   	<div class="header">
		   		<h2>{title}</h2>
		   		<!-- Desktop / Tablet -->
		   		<span class="close" onclick={closeMenu}></span>
		   		
		   	</div>
		   	
		   	<div id="menuContent"></div>
	   	</div>
   	</div>
   	
   	<script>
    var self   = this,
        action = require("./action.js");
   	
    self.on("update", function() {
        // Convert newlines to <br /> tags
        self.menuContent.innerHTML = self.content && self.content.replace(/\n/g, "<br />");
    });
   	
   	closeMenu() {
        self.opts.onclose && self.opts.onclose.call();
   	}
   	
   	// Close the menu with the ESC key
   	closeMenuOnESC(e) {
   		if(e.which == 27) {
   			self.closeMenu();
   		}
   	}
   	
   	// Close the menu if you click on the overlay (but not one of its children)
   	closeMenuOnOverlayClick(e) {
   		if(e.target === self.menuOverlay) {
   			self.closeMenu();
   		}   
        
        // Let the browser handle the event if this is a link inside the menu
        return true;		
   	}
   	</script>
</menu-overlay>
