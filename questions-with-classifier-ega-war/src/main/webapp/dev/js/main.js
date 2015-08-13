/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

// Main entry point to our application.  Register the data stores with Dispatcher and mount the main home tag.

var riot              = require("riot"),
    Polyglot          = require("node-polyglot"),
    Dispatcher        = require("./Dispatcher.js"),
    ConversationStore = require("./ConversationStore.js"),
    PersistenceStore  = require("./PersistenceStore.js"),
    RoutingStore      = require("./RoutingStore.js"),
    MenuStore         = require("./MenuStore.js");


// Initialize our data stores
    
var conversationStore = new ConversationStore(),
    persistenceStore  = new PersistenceStore(),
    routingStore      = new RoutingStore(),
    menuStore         = new MenuStore();
    
    
// Get Dispatcher all set up
    
Dispatcher.addStore(conversationStore);
Dispatcher.addStore(persistenceStore);
Dispatcher.addStore(routingStore);
Dispatcher.addStore(menuStore);


// User language detection and internationalization

var browserLang = navigator.language || navigator.userLanguage;

complLang = browserLang.split('-');

lang    = complLang[0];
dialect = complLang[1];

if(lang === "ru") {
  var stringBundleAddress = './strings/ru.json';
}
else {
  var stringBundleAddress = './strings/en.json';
}


// Routing
// Clear existing routing callbacks

riot.route.stop();
riot.route.start();

fetch(stringBundleAddress)
    .then(function(response) {
      if (response.status >= 200 && response.status < 300) {
        return Promise.resolve(response);
      } else {
        return Promise.reject(new Error(response.statusText));
      }
    })
    .then(function(response) {
      return response.json();
    })
    .then(function(data) {
        polyglot  = new Polyglot({phrases: data});
    }).catch(function(error) {
        console.log('Language bundle request failed', error);
    })
    .then(function(data) {
        riot.mount("app");
    });