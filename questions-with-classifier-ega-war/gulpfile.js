/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

var gulp        = require('gulp'),
    browserify  = require('browserify'),
    runSequence = require('run-sequence').use(gulp),
    plugins     = require('gulp-load-plugins')(),
    through2    = require('through2'),
    del         = require('del'),
    karma       = require('karma').server,
    config      = 
        {
         "src"        : source     = __dirname + "/src",
         "main"       : main       = source    + "/main",
         "workdir"    : workdir    = main      + "/webapp",
         "test"       : test       = source    + "/test",
         "jstest"     : jstest     = test      + "/javascript",
         "target"     : target     = __dirname + "/target",
         "temp"       :              target    + "/webtemp",
         "testTarget" : testTarget = target    + "/client-reports",
         "bowerDir"   : bowerDir   = __dirname + "/bower_components",
         "riotSrc"    :              bowerDir  + "/riot",
         "fetchSrc"   :              bowerDir  + "/fetch",
         "promise"    :              bowerDir  + "/es6-promise",
         "bootstrap"  :              bowerDir  + "/bootstrap-sass",
         "webdev"     : webdev     = workdir   + "/dev",
         "sass"       :              webdev    + "/sass",
         "tag"        :              webdev    + "/tag",
         "jsDev"      :              webdev    + "/js",
         "js"         :              workdir   + "/js",
         "css"        :              workdir   + "/css",
         "html"       :              workdir   + "/html",
        };


// Sass compilation
gulp.task("sass", function(callback) {
    runSequence("copy_sass_to_temp", "compile_sass", callback);
});

gulp.task("compile_sass", function() {
    return gulp.src(config.temp + "/*.scss")
        .pipe(plugins.sass())
        .pipe(gulp.dest(config.css));
});

gulp.task("copy_sass_to_temp", function() {
    var sassFiles = gulp.src(config.sass + "/*.scss")
        .pipe(gulp.dest(config.temp));
    
    var bootstrap = gulp.src(config.bootstrap + "/assets/stylesheets/**/*")
        .pipe(gulp.dest(config.temp));
    
    return plugins.merge(sassFiles, bootstrap);
});



// Code compilation
gulp.task("riot", function() {
    var main = gulp.src(config.jsDev + "/main.js"),
        tags = gulp.src(config.tag + "/*.tag")
        .pipe(plugins.riot())
        .pipe(plugins.concat("tags.js"))
        .pipe(gulp.dest(config.temp));
        
    return plugins.merge(main, tags)
        .pipe(plugins.order([
            "main.js",
            "tags.js"
        ]))
        .pipe(plugins.concat("main.js"))
        .pipe(gulp.dest(config.temp));
});



// Dev helpers
gulp.task("browserify", function() {
    
    function browserifySetup(file, end, next) {
        browserify(file.path, { debug: process.env.NODE_ENV === 'development' })
            .bundle(function (err, res) {
                if (err) { 
                    return next(err); 
                }

                file.contents = res;
                next(null, file);
            });
    }
    
    return gulp.src(config.temp + "/main.js")
        .pipe(through2.obj(browserifySetup))
        .pipe(plugins.rename("bundle.js"))
        .pipe(gulp.dest(config.temp));
});

gulp.task("combine_scripts_polyfills", function() {
    var polyfills = gulp.src(config.temp + "/polyfill.js"),
        bundle    = gulp.src(config.temp + "/bundle.js");
    
    return plugins.merge(polyfills, bundle)
        .pipe(plugins.order([
            "polyfill.js",
            "bundle.js"
        ]))
        .pipe(plugins.concat("bundle.js"))
        .pipe(gulp.dest(config.js));
});

gulp.task("lint", function() {
    return gulp.src(config.jsDev + "/*.js")
        .pipe(plugins.jshint())
        .pipe(plugins.jshint.reporter("default"));
});

gulp.task("copy_scripts", function() {
    return gulp.src([config.jsDev + "/*.js",
            "!" + config.jsDev + "/main.js"])
        .pipe(gulp.dest(config.temp));
});

gulp.task("polyfill", function() {
    var promiseSource = gulp.src(config.promise + "/promise.min.js");     
    var fetchSource = gulp.src(config.fetchSrc + "/fetch.js");
    
    return plugins.merge(promiseSource, fetchSource)
        .pipe(plugins.order([
            "promise.min.js",
            "fetch.js"
        ]))
        .pipe(plugins.concat("polyfill.js"))
        .pipe(gulp.dest(config.temp));
});

gulp.task("uglify", function() {
    return gulp.src([config.js + "/bundle.js"])
        .pipe(plugins.uglify())
        .pipe(gulp.dest(config.js));
});

gulp.task("watch_tags", function() {
    return gulp.watch(config.tag + "/*.tag", ["compile_debug"]);
});

gulp.task("watch_sass", function() {
    return gulp.watch(config.sass + "/*.scss", ["sass"]);
});

gulp.task("clean_all", function() {
    return del([
        config.temp,
        config.js + "/*.js",
        "!" + config.js + "/modernizr.js",
        config.css + "/*.css"
    ]);
});

gulp.task("cleanup_postbuild", function() {
    return del(config.temp);
});

gulp.task("test_ci", function(done) {
    return karma.start({
        configFile: config.jstest + "/karma.conf.js",
        singleRun: false,
      browsers: ["Chrome"]
    }, done);
});

gulp.task("test", function(done) {
    return karma.start({
        configFile: config.jstest + "/karma.conf.js",
      junitReporter: {
         outputFile: config.testTarget + "/TEST-client-results.xml"
      }
    }, done);
});

gulp.task("watch", function(callback) {
    runSequence(["watch_tags", "watch_sass"], "test_ci", callback);
});

gulp.task("compile_production",   function(callback) {
    runSequence("clean_all", "sass", "riot", "copy_scripts", "browserify", "polyfill", "combine_scripts_polyfills", "uglify", "cleanup_postbuild", callback);
});

gulp.task("compile_debug",   function(callback) {
    runSequence("clean_all", "sass", "riot", "copy_scripts", "browserify", "polyfill", "combine_scripts_polyfills", "lint", "cleanup_postbuild", callback);
});

gulp.task("build",       ["compile_production"]);
gulp.task("build_debug", ["compile_debug"]);

gulp.task("default",   ["build_debug"]);

