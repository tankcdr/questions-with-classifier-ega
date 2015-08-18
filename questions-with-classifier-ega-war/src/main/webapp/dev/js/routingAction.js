/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

// For my sanity, please keep this alphabetical
var routingAction = {
    "ASK_QUESTION"               : "a_question_has_been_asked",
    "CONVERSATION_STARTED"       : "new_conversation_has_been_started",
    "REFINEMENT_REQUESTED"       : "direct_to_refinement_questions",
    "SHOW_HOME_PAGE"             : "show_the_home_page",
    "SHOW_HOME_PAGE_BROADCAST"   : "show_the_home_page_broadcast_action"
};

if (typeof(module) !== 'undefined') module.exports = routingAction;