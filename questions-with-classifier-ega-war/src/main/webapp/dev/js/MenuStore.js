/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var riot      = require("riot"),
    action    = require("./action.js"),
    constants = require("./constants.js");

function MenuStore() {
    riot.observable(this);

    var self = this;
    
    self.on(action.SHOW_MENU_OVERLAY, function(menu) { 
        self.trigger(action.SHOW_MENU_OVERLAY_BROADCAST, menu); 
    });
}

if (typeof(module) !== 'undefined') module.exports = MenuStore;