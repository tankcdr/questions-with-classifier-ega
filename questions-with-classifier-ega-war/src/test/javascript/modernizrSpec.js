/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

describe("Modernizr tests", function() {
	it("Should be defined", function(){
        expect(Modernizr).toBeDefined();
	});
    
    it("Modernizr is testing for SMIL support", function() {
        expect(Modernizr.smil).not.toBeNull();
    });
    
    it("Modernizr is testing for LocalStorage support", function() {
        expect(Modernizr.localstorage).not.toBeNull();
    });
});
