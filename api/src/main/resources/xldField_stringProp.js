var XldField = require('XldField');


XldField_stringProp = function(fieldName, size) {
	XldField.call(this, fieldName);
	
	var _size = parseInt(size);
	
	this.size = function(x) {
		if (parseInt(x) > 0)
			_size = x;
		return size;
	}


}
XldField_stringProp.prototype = Object.create(XldField.prototype); 
XldField_stringProp.prototype.constructor = XldField_stringProp;

XldField_stringProp.prototype.toString = function() {
	return XldField.prototype.toString.call(this) + '-txtProp('+this.size()+')';
}


module.exports = XldField_stringProp;