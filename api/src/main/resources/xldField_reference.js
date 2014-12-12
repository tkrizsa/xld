var XldField = require('XldField');
var xld = require('xld');


XldField_reference = function(fieldName, params) {
	XldField.call(this, fieldName);
	
	if (!params.modelId) {
		xld.log(params);
		throw "No modelId in params of reference field!";
	}
	
	this.refModelId = params.modelId;

}
XldField_reference.prototype = Object.create(XldField.prototype); 
XldField_reference.prototype.constructor = XldField_reference;

XldField_reference.prototype.toString = function() {
	return XldField.prototype.toString.call(this) + '-reference';
}


module.exports = XldField_reference;