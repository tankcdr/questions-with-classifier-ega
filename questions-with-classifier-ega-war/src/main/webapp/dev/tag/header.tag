/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<header>
    <nav class="navbar navbar-default" role="navigation">
        <div class="header-block">
            <div class="header-title-block">
                <watson-logo state="listening" color="#fff"></watson-logo>
                <div id="app-heading" class="header-text"></div>
            </div>
        </div>
        
        <custom-menu class="menu-icon"></menu>
    </nav>
<script>
	this["app-heading"].innerHTML = polyglot.t("titleHTML");
</script>
</header>
