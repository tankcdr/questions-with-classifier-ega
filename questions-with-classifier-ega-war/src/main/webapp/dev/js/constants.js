/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var constants = {
    conversationUrl : "/api/v1/conversation/",
    feedbackUrl     : "/api/v1/feedback/",
    visitLevels     : Object.freeze({
        NONE    : 0,
        WELCOME : 1,
        POPUP   : 2
    }),
    showPopupCount          : 3,
    visitLevelKey           : "visitLevel",
    refinementQueryType     : "REFINEMENT",
    needHelpFeedbackType    : "NEED_HELP",
    topQuestionFeedbackType : "TOP_QUESTION"
};

if (typeof(module) !== 'undefined') module.exports = constants;