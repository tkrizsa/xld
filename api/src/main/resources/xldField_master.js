var XldField = require('XldField');
var xld = require('xld');


XldField_master = function(fieldName, params) {
	XldField.call(this, fieldName);
	
	if (!params.modelId) {
		xld.log(params);
		throw "No modelId in params of master field!";
	}
	
	this.refModelId = params.modelId;

}
XldField_master.prototype = Object.create(XldField.prototype); 
XldField_master.prototype.constructor = XldField_master;

XldField_master.prototype.toString = function() {
	return XldField.prototype.toString.call(this) + '-master';
}


module.exports = XldField_master;