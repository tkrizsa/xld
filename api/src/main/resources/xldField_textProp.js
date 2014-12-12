var XldField = require('XldField');


XldField_textProp = function(fieldName) {
	XldField.call(this, fieldName);
	

}
XldField_textProp.prototype = Object.create(XldField.prototype); 
XldField_textProp.prototype.constructor = XldField_textProp;

XldField_textProp.prototype.toString = function() {
	return XldField.prototype.toString.call(this) + '-txtProp';
}


module.exports = XldField_textProp;