var xld = require('xld.js');
var Model = require('xldModel');


var Partner = function() {
	Model.call(this);
	
	
	this.modelId = 'partner';
	this.tableName('partner');
	this.fieldAdd('partnerId', 'id');
	this.fieldAdd('partnerName', 'stringProp', 100);
	this.fieldAdd('partnerStatus', 'enumProp', ['programmer', 'customer']);
	this.fieldAdd('address1', 'stringProp', 200);
	this.fieldAdd('address2', 'stringProp', 200);

	
};
Partner.prototype = Object.create(Model.prototype); 
Partner.prototype.constructor = Partner;





module.exports = Partner;