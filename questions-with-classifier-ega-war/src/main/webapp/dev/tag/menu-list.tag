/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<menu-list>
  <div class="close-menu-overlay hidden" onclick={toggle}></div>
	<ul class="menu-options visible">
   		<li each={ option, i in menuOptions } class="menu-option-{option.id}" onmousedown={ parent.itemClicked }>
   			<h3>{option.title}</h3>
   		</li>
        <powered-by class="poweredBy-menu"></powered-by>
    </ul>
    <div class="menu-content">
        <a class="menu-title" href="#"  onmousedown={ backClicked }>{menu.title}</a>
        <p id="menuContent" class="menu-text"></p>
    </div> 
	<script>
    var self   = this,
    action = require("./action.js");
    
    self.menuOptions = [
      {
          id: "home",
          title: polyglot.t("menu-home"),
          action: function() {
              location.reload();
          }
      },
      {
          id: "documentation",
          title: polyglot.t("menu-doc"),
          action: function() {
              window.open("http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/nl-classifier/", "_blank");
          }
      },
      {
          id: "about",
          title: polyglot.t("menu-about"),
          content: polyglot.t("menu-about-content")
      },
      {
          id: "terms",
          title: polyglot.t("menu-terms"),
          content: polyglot.t("menu-terms-content")
      }
    ];

    toggle() {
        self.opts.menutoggle && self.opts.menutoggle.call();
    }
    
    self.menu = {
        title:"",
        content:""
    };

    itemClicked(e) {
        if(!e.item.option.content) {
            e.item.option.action();
        }
        else {
            self.menu.title            = e.item.option.title;
            self.menu.content          = e.item.option.content;
            self.menuContent.innerHTML = e.item.option.content;
            self.root.children[1].classList.remove("visible");
            self.root.children[2].classList.toggle("active");
            self.update();
            
            Dispatcher.trigger(action.SHOW_MENU_OVERLAY, self.menu);
        }
    }

    backClicked() {
        self.root.children[1].classList.add("visible");
        self.root.children[2].classList.remove("active");
        self.update();
    }

  </script>
</menu-list>
