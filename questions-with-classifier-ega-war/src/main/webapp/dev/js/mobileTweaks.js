/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */


//Hiding keyboard after input 
var hideKeyboard = function() {
	document.activeElement.blur();
	var inputs = document.querySelectorAll('input');
	for(var i=0; i < inputs.length; i++)
		inputs[i].blur();
};