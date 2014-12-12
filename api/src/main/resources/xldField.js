var xld = require('xld.js');

XldField = function(fieldName) {
	
	var _fieldName = fieldName;
	
	this.fieldName = function(x) {
		if (typeof x == 'string')
			_fieldName = x;
		return _fieldName;
	}
	
	this.isKey = function() {
		return false;
	}
	
}

XldField.prototype.toString = function() {
	return this.fieldName();
}

XldField.prototype.toSqlValue = function(val) {
	return "'" + val + "'";
}


module.exports = XldField;