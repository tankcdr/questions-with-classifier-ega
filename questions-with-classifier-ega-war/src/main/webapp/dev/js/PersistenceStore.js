/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var riot      = require("riot"),
    action    = require("./action.js"),
    constants = require("./constants.js");

function PersistenceStore() {
    riot.observable(this);
  
    var self = this;
    
    // Web Storage polyfill
    if(!Modernizr.localstorage) {
        window.localStorage = {
          _data       : {},
          setItem     : function(id, val) { this._data[id] = String(val); },
          getItem     : function(id) { return this._data.hasOwnProperty(id) ? this._data[id] : undefined; },
          removeItem  : function(id) { return delete this._data[id]; },
          clear       : function() { this._data = {}; }
        };
    }
    
    
    // Observable methods
        
	/**
	 * Sets the visit level stored in localStorage
	 * @param  {enum}  visit from list of visitLevels
	 */
    self.on(action.SET_VISIT_LEVEL, function(visitLevel) {
        
		switch(visitLevel){
            case constants.visitLevels.NONE:
                localStorage.setItem(constants.visitLevelKey, constants.visitLevels.NONE.toString(10));
                break;
			case constants.visitLevels.WELCOME:
				localStorage.setItem(constants.visitLevelKey, constants.visitLevels.WELCOME.toString(10));
				break;
			case constants.visitLevels.POPUP:
				localStorage.setItem(constants.visitLevelKey, constants.visitLevels.POPUP.toString(10));
				break;
			default:
                localStorage.setItem(constants.visitLevelKey, constants.visitLevels.NONE.toString(10));
				break;
		}
        
        self.trigger(action.VISIT_LEVEL_CHANGED_BROADCAST);
    });
    
	/**
	 * Get and broadcast the current visit level
	 */
    self.on(action.GET_VISIT_LEVEL, function() {
        
        var visitLevel = parseInt(localStorage.getItem(constants.visitLevelKey), 10);
        
		switch(visitLevel){
    		case constants.visitLevels.NONE:
    			self.trigger(action.VISIT_LEVEL_NONE_BROADCAST, visitLevel);
    			break;
			case constants.visitLevels.WELCOME:
				self.trigger(action.VISIT_LEVEL_WELCOME_BROADCAST, visitLevel);
				break;
			case constants.visitLevels.POPUP:
				self.trigger(action.VISIT_LEVEL_POPUP_BROADCAST, visitLevel);
				break;
			default:
                // The case that there is no visit level (probably NaN), so set one and return our default
                visitLevel = constants.visitLevels.NONE;
                self.trigger(action.SET_VISIT_LEVEL, visitLevel);
                self.trigger(action.VISIT_LEVEL_NONE_BROADCAST, visitLevel);
				break;
		}
        
        self.trigger(action.VISIT_LEVEL_BROADCAST, visitLevel);
    });
    
}

if (typeof(module) !== 'undefined') module.exports = PersistenceStore;