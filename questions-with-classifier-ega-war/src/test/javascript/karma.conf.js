/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

// This file is configured for one time mode, not development. 
// To use for development override these configurations.
//
// T. Dalbo, 7/10/2015:  The karma-riot plugin is broken, so riot preprocessing does not work
//                       For now, all tests are disabled until it has been updated.
module.exports = function(config) {
    config.set({

        basePath: "../../",

        frameworks: ['jasmine' /*, 'riot'*/],

        files: [
            'test/javascript/polyfills.js',
            'main/webapp/js/modernizr.js',
            /*'main/webapp/dev/js/*.js',
            'main/webapp/dev/tag/*.tag,*/
            'test/javascript/*Spec.js'
        ],

        /*preprocessors: {
            'main/webapp/dev/tag/*.tag': ['riot']
        },*/

        reporters: ["progress","junit"],

        // Please override this if karma.conf.js is ever moved.  Nasty but necessary
        // due to how frontend-maven-plugin works.  If it ever allowed config parameters,
        // remove this entirely.
        junitReporter: {
        outputFile: "../target/client-reports/TEST-client-results.xml"
        },

        port: 9876,

        colors:true,

        logLevel: config.LOG_INFO,

        browsers: ["PhantomJS"],

        plugins: [
            'karma-phantomjs-launcher',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-junit-reporter',
            'karma-jasmine',
            'karma-riot'
        ],

        // Disable continuous integration.  This functionality will be implemented in gulp.watch
        singleRun: true
    });
};
