/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

// For my sanity, please keep this alphabetical
var routingAction = {
    "ASK_QUESTION"               : "a_question_has_been_asked",
    "CONVERSATION_STARTED"       : "new_conversation_has_been_started",
    "REFINEMENT_REQUESTED"       : "direct_to_refinement_questions"
};

if (typeof(module) !== 'undefined') module.exports = routingAction;