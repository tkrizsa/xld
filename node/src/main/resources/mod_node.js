var vertx = require('vertx');
var console = require('vertx/console');
var container = require('vertx/container');
var eb = vertx.eventBus;
var xld = require('xld');
var rm = require('xldRouteMatcher');



xld.log('', '','', '=============================== XLD Started... =================================', '', '');




// ===================================== RUN SERVER =====================================


var addRoute = function(method, pattern, address, module) {

	rm.register({method : method, pattern : pattern, address : address, module : module});
	//console.log('add pattern: "'+pattern+'"; address: "'+address+'"; module: "'+ module + "'");
}



var registeredPatterns = {};

var regFunc = function(a) {
	method = 'get';
	if (a.method) {
		method = a.method.toLowerCase();
	}
	if (registeredPatterns[method + '|' + a.pattern]) {
		throw "PATTERN ALREADY REGISTERED! ["+method + '|' + a.pattern + "]";
	}
	registeredPatterns[method + '|' + a.pattern] = a.address;
	addRoute(method, a.pattern, a.address, a.module);
	if (a.kind == 'template') {
		if (registeredPatterns['get|' + a.indexPattern]) {
			throw "PATTERN ALREADY REGISTERED!";
		}
		addRoute('get', a.indexPattern, '_index', a.module);
	}
	
}	

eb.registerHandler('xld-register-http', regFunc);



var server = vertx.createHttpServer();
server.ssl(true);
server.keyStorePath('xldata.jks');
server.keyStorePassword('qwert1978');

server.requestHandler(function(request) {
		console.log('HTTP ' + request.method() + ' ' + request.uri());
		x = rm.check(request.method(), request.path());
		if (!x) {
			request.response.statusCode(404).statusMessage('No route found').end('No route for ' + request.method() + ' ' + request.uri());
			return;
		}
		//xld.log('found pattern: "'+x.route.pattern);
		var r = {};
		r.params = {};
		for (var i in x.params) {
			r.params[i] = x.params[i];
		}
		request.params().forEach(function(k, val) {
			r.params[k] = val;
		});
		
		r.path = request.path();
		var addr = x.route.address;
		if (addr == '_index') {
			addr = registeredPatterns['get|/'];
			r.path = '/';
		}

		
		if (x.route.method == 'put' || x.route.method == 'post') {
			// should be dangerous in case of large uploaded body, whole body kept in memory!
			request.bodyHandler(function(body) {
				r.body = body.toString();
				eb.send(addr, r, function(reply) {
					if (reply.status) {
						request.response.statusCode(reply.status);
					}
					if (reply.contentType) {
						request.response.putHeader('Content-Type', reply.contentType);
					}
					
					request.response.end(reply.body);
				});
			});		
		} else {
			eb.send(addr, r, function(reply) {
				if (reply.status) {
					request.response.statusCode(reply.status);
				}
				if (reply.contentType) {
					request.response.putHeader('Content-Type', reply.contentType);
				}
				if (request.headers().get('Range')) 
					request.response.statusCode(206);
				request.response.end(reply.body);
			});
		}




});

server.listen(8080, '0.0.0.0');

xld.http('/parseUrls', function(req, replier) {
	var serr = '';
	var modules = {};
	var urls = JSON.parse(req.params.urls);
	for (var i in urls) {
		var url = urls[i];
		var p = rm.check('get', url);
		
		if (p && p.route.module) {
			modules[p.route.module] = true;
		} else {
			xld.log('parseurl not found : ' + url, p);
			serr += 'Server dont find module for route : ' + url + "\r\n";
		}
	}
	if (serr) {
		replier({body : serr, status : 400});
		return;
	}
	var mc = 0;
	var mfs = [];
	for (var m in modules) {
		var mp = rm.check('get', '/module/' + m);
		if (!m) {
			xld.log("No route registered for module '" + m + "'");
			continue;
		}
		mc++;
		eb.send(mp.route.address, null, function(reply) {
			//xld.log("module answer:", reply);
			var mf = JSON.parse(reply.body);
			for (var i in mf) {
				mfs.push(mf[i]);
			}
			mc--;
			if (mc <= 0) {
				replier({body : JSON.stringify(mfs), contentType : 'application/json'});
			}
		
		});
	
	}
	
});



// ========================== LOAD MODULES ===========================
var config = {};


var sqlConfig = {
  "address" : 'xld-sql-persist',
  "connection" 		: 'MySQL',
  "host" 			: 'localhost',
  "port" 			: 3306,
  "maxPoolSize" 	: 10,
  "username" 		: 'root',
  "password" 		: 'qwert1978',
  "database" 		: 'flow'
}

//container.deployModule("io.vertx~mod-mysql-postgresql~0.3.0-SNAPSHOT", 	sqlConfig, function(err, deployID) {
container.deployModule("io.vertx~mod-mysql-postgresql_2.11~0.3.1", 	sqlConfig, function(err, deployID) {
	if (err) {
		console.log("Deployment failed! " + err.getMessage());
	} else {
		// container.deployModule("xld~auth~1.0", 					config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		container.deployModule("xld~site~1.0", 						config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		container.deployModule("xld~api~1.0", 						config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		container.deployModule("xld~api-actor~1.0", 				config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		container.deployModule("xld~api-article~1.0",		 		config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		container.deployModule("xld~api-order~1.0",			 		config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		
		container.deployModule("xld~api-homebudget~1.0",			config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
		
		// only for opening page
		container.deployModule("xld~api-partner~1.0",		 		config, function(err, deployID) {if (err) {console.log("Deployment failed! " + err.getMessage());}});
	}

	
});


