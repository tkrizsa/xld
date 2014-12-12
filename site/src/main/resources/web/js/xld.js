var xld = xld || {};


xld.getStruct = function (page, url, scope) {
	var s = new xld.Struct(this, url, scope);
	return s;
}


xld.Struct = function(owner, url, scope) {
	this._owner = owner;
	this._url = url;
	this._scope = scope;
	this._rows = false;

	var _thans = [];
	var _errs = [];
	
	
	var thisStruct = this;
	
	
	this.reload = function() {
		$.ajax({
			url : url, 
			success : function(resp) {
				thisStruct._rows = resp.rows;
				if (resp.templates)
					thisStruct.templates = resp.templates;
				if (thisStruct._rows.length>0) {
					$.each(thisStruct._rows[0], function(fn, val) {
						thisStruct[fn] = val;
					});
				
				}
			
			
				$.each(_thans, function(i, func) {
					var ret = func(thisStruct);
					if (thisStruct._scope) {
						thisStruct._scope.$apply();
					}
				});
				
				
				if (_thans.length == 0 && thisStruct._scope) {
					thisStruct._scope.$apply();
				}
				
			},
			error : function(resp) {
				new jBox('Notice', {
					content : resp.responseText
				});
			}
		});
	}
	
	this.reload();
	
	this.than = function(func) {
		_thans.push(func);
		return this;
	}
	
	this.err = function(func) {
		_errs.push(func);
		return this;
	}
	
	this.save = function(func) {
		$.ajax({
			type : 'post',
			url : this._url, 
			data : JSON.stringify({rows : this._rows}), 
			success : function(resp) {
				if (typeof func == 'function') {
					if (thisStruct._scope) {
						thisStruct._scope.$apply(function() {
							func();
						});
					} else {
						func();
					}
				}
			},
			error : function(resp) {
				new jBox('Notice', {
					content : resp.responseText,
					attributes: {
						x: 'left',
						y: 'bottom'
					},
					position: {  // The position attribute defines the distance to the window edges
						x: 50,
						y: 5
					},
					color : 'red',
					theme: 'NoticeBorder'
				});
			}
		});
	
	}
}