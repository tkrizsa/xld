var xld = require('xld');


var xldRouteMatcher = function() {

	var routes = [];


	this.register = function(p) {
		
		if (!p.method)
			p.method = 'get';
		else 
			p.method = p.method.toLowerCase();
	
		var pattern = p.pattern;
		p.paramNames = [];
		
		while (pattern.substr(0,1) == '/')
			pattern = pattern.substr(1);
		
		var pata = pattern.split('/');
		var pat = '^';
		for(var i in pata) {
			var pp = pata[i];
			pat += '\\/?';
			if (pp.substr(0,1) != ':') {
				pat += pp.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
			} else {
				p.paramNames.push(pp.substr(1));
				pat += '([a-zA-Z0-9_\\-\\.]*)';
			}
		
		}
		pat += '\\/?$';
		p.regExp = new RegExp(pat, 'i');
		
		routes.push(p);
	
	}

	this.check = function(method, path) {
		method = method.toLowerCase();
		for (var i in routes) {
			p = routes[i];
			if (p.method && p.method != method)
				continue;
			
			var res = false;
			var m = path.match(p.regExp);
			if (m == null) 
				continue;

			res = {};
			for (var i in p.paramNames) {
				res[p.paramNames[i]] = m[parseInt(i)+1];
			}
			return {
				route : p,
				params : res
			}
		
		}
	
		return false;
	}

}

module.exports = new xldRouteMatcher()