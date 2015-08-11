/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

// Main entry point to our application.  Register the data stores with Dispatcher and mount the main home tag.

var riot              = require("riot"),
    Polyglot          = require("node-polyglot"),
    Dispatcher        = require("./Dispatcher.js"),
    ConversationStore = require("./ConversationStore.js"),
    PersistenceStore  = require("./PersistenceStore.js"),
    MenuStore         = require("./MenuStore.js");

    
var conversationStore = new ConversationStore(),
    persistenceStore  = new PersistenceStore(),
    menuStore         = new MenuStore();
    
Dispatcher.addStore(conversationStore);
Dispatcher.addStore(persistenceStore);
Dispatcher.addStore(menuStore);

var browserLang = navigator.language || navigator.userLanguage;

complLang = browserLang.split('-');

lang = complLang[0];
dialect = complLang[1];

if(lang === "ru") {
  var jsonAddr = './strings/ru.json';
}
else {
  var jsonAddr = './strings/en.json';
}

fetch(jsonAddr)
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
        console.log('Request failed', error);
    })
    .then(function(data) {
        riot.mount("app");
    });