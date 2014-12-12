var xld = require('xld');
var XldField = require('XldField');


XldField_enumProp = function(fieldName, options) {
	var _super = XldField.call(this, fieldName);
	
	var _options = options;
	
	this.options = function(x) {
		if (typeof x == 'array')
			_options = x;
		return _options;
	}

	
}
XldField_enumProp.prototype = Object.create(XldField.prototype); 
XldField_enumProp.prototype.constructor = XldField_enumProp;

XldField_enumProp.prototype.toString = function() {
	return XldField.prototype.toString.call(this) + '-enumProp('+this.options().join(';')+')';
}


module.exports = XldField_enumProp;