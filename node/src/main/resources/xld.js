var vertx = require('vertx');
var console = require('vertx/console');
var eb = vertx.eventBus;


var Node = function() {

	var thisNode = this;
	this.moduleName = false;
	this.PATH_DELIMITER = '\\';
	this.log = function() {
	
		for (var i in arguments) {
			var x = arguments[i];
			if (typeof x == 'undefined') {
				console.log('undefined');
			} else if (typeof x == 'object') {
				try {
					console.log(JSON.stringify(x, null, 5));
				} catch(e) {
					console.log(x);
				}
			} else {
				console.log(x);
			}
		}
	}


	this.api = function(pattern, func) {
		var address = pattern + '_' + Math.random();
		eb.publish('xld-register-http', {
			kind 	: 'api',
			pattern : pattern,
			address : address
		});
		
		eb.registerHandler(address, function(req, replier) {
			func(req, function(obj, err, objType) {
				
				if (!err) {
					var ot = 'application/xld+json';
					if (objType)
						ot = 'application/xld.'+ objType +'+json';
					replier({
						body 			: JSON.stringify(obj),
						contentType 	: ot
					});
				} else {
					replier({
						body 			: err,
						status			: 400,
						contentType 	: 'text/plain'
					});
				}
			});
		});
	}

	this.apiPost = function(pattern, func) {
		var address = pattern + '_' + Math.random();
		eb.publish('xld-register-http', {
			kind 	: 'api',
			pattern : pattern,
			address : address,
			method  : 'post'
		});
		
		eb.registerHandler(address, function(req, replier) {
			func(req, function(obj, err, objType) {
				if (!err) {
					var ot = 'application/xld+json';
					if (objType)
						ot = 'application/xld.'+ objType +'+json';
					replier({
						body 			: JSON.stringify(obj),
						contentType 	: ot
					});
				} else {
					replier({
						body 			: err,
						status			: 400,
						contentType 	: 'text/plain'
					});
				}
			});
		});
	}

 
	this.http = function(pattern, func) {
		var address = pattern + '_' + Math.random();
		eb.publish('xld-register-http', {
			kind 	: 'site',
			pattern : pattern,
			address : address
		});
		
		eb.registerHandler(address, function(req, replier) {
			func(req, function(reply) {
				replier(reply);
			});
		});
	}


	this.template = function(templatePattern, indexPattern, fileName) {
		if (!this.moduleName)
			throw "No module name set"; 
	
		if (typeof fileName != 'string')		fileName = templatePattern;
		if (typeof indexPattern != 'string')	indexPattern = templatePattern;
		var address = 'template_'  + templatePattern + '_' + Math.random();
		eb.publish('xld-register-http', { 
			module  : this.moduleName,
			kind 	: 'template',
			pattern : '/templates/'+templatePattern,
			indexPattern : '/'+indexPattern,
			address : address
		});
		
		eb.registerHandler(address, function(req, replier) {
			vertx.fileSystem.readFile('client/'+fileName +'.html', function(err, res) {
				if (!err) {
					var x = res.toString();
					var ct = 'text/html';
					replier({body : x, contentType : ct});
				} else {
					replier({body : '404 template not found', status : 404});
				}
			});			
		});
	}


	// ====================================================== MODULE REGISTER ====================================================
	
	this. moduleRegister = function(moduleName) {
		this.moduleName = moduleName;
		this.http('/module/' + moduleName, this.responseModuleGet);
		
	}
	
	
	this.responseModuleGet = function(req, replier) {
		vertx.fileSystem.readDir('client/', function(err, res) {
			if (err) {
				thisNode.log(err); 
				replier({body : '500 cannot read dir', status : 500});
				return;
			}
			var fc = res.length;
			var serr = '';
			var mf = [];
			
			var myReadFile = function(f, func) {
				vertx.fileSystem.readFile(f, function(err, res) {
					func(err, res, f);
				});
			}
			
			for (var i = 0; i < res.length; i++) {
				var f = res[i];
				myReadFile(f, function(err, res, f) {
					if (!err) {
						
						var x = res.toString();
						var file = (f.split(thisNode.PATH_DELIMITER).slice(-1)[0]);
						file = file.split('.')[0];
						var ext = f.split('.').slice(-1)[0];
						var params = thisNode.extractClientFileParams(x);
						//thisNode.log(file, '------------ params ---------------', params);
						if (ext == 'html') {
							mf.push({
								kind : 'template',
								templateName : '/templates/'+file,
								module : thisNode.moduleName,
								body : x
							});
						} else if (ext == 'js' && params['xld-parser']) {
							mf.push({
								kind : 'parser',
								module : thisNode.moduleName,
								body : x
							});
						} else if (ext == 'js' && params['xld-controller']) {
							mf.push({
								kind : 'controller',
								name : params['xld-controller'],
								module : thisNode.moduleName,
								body : x
							});
						}
					
					} else {
						thisNode.log(err); 
						serr += err + '\r\n';
					}
					fc --;
					if (fc <= 0) {
						if (serr) {
							replier({body : '500 cannot read file\r\n'+serr, status : 500});					
						} else {
							replier({body : JSON.stringify(mf), contentType : 'application/json'});
						}
					}
				});
			}
		});
	}	

	this.extractClientFileParams = function(file) {
		var params = {};
		var lines = file.replace(/\r/gm, "").split("\n");
		for (var l in lines) {
			var line = lines[l];
			if (line.indexOf('//@xld') === 0) {
				var keyval = line.substr(3).trim();
				var p = keyval.indexOf(':');
				var key, val;
				if (p>=0) {
					key = keyval.substr(0,p).trim();
					val = keyval.substr(p+1).trim();
				} else {
					key = keyval;
					val = true;
				}
				params[key] = val;
			
			}
		
		}
		return params;
	}

}

module.exports = new Node();