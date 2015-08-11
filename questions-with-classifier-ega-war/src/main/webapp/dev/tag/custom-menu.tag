/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<custom-menu>
	<div id="menuIconContainerDesktop" class="menuIconContainer">
		<span class="menu-label">{menuLabel}</span>
		<span class="menuIconImg" onclick={toggleMenu}></span>
	</div>
	<menu-list class="menu-list" menutoggle={toggleMenu}></menu-list>
	
	<script>
    
	var self           = this,
	action             = require("./action.js");
	this.showMenuList  = true,
	this.menuLabel     = polyglot.t("menu");

    toggleMenu() {
        self.menuIconContainerDesktop.classList.toggle("active");
		document.querySelector(".close-menu-overlay").classList.toggle("hidden");
		document.querySelector("menu-list").classList.toggle("active");
		document.querySelector("body").classList.toggle("push-toleft");
		setTimeout(function(){ 
			document.querySelector(".menu-options").classList.add("visible");
			document.querySelector(".menu-content").classList.remove("active");
		}, 400);
    }
    
	</script>
</custom-menu>
