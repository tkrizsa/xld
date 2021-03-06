var vertx = require('vertx');

var xld = require('xld.js');


xld.log('XLD site started ......');






var fileLoader = function(req, replier) {
	var f = 'index.html';
	if (req.path && req.path != '/')
		f = req.path;
	xld.log('file:');
	xld.log(f);
	vertx.fileSystem.readFile('web/'+f, function(err, res) {
		if (!err) {
			var x = res.toString();
			var ct = 'text/plain';
			var ext = f.split('.').slice(-1)[0];
			
			switch (ext) {
				case 'html' 	: ct = 'text/html';
				break;
				case 'css' 		: ct = 'text/css';
				break;
				case 'js' 		: ct = 'text/javascript';
				break;
				case 'png' 		: ct = 'image/png';
				break;
				case 'mp3' 		: ct = 'audio/mpeg';
				break;
			
			}
			
			replier({body : x, contentType : ct});
		} else {
			replier({body : '404 not found', status : 404});
		}
	});
};


var root = "d:/work2/xld/mods/xld~site~1.0/web/";

xld.http('/', fileLoader);
xld.httpFile('/favicon.ico', 		root+"favicon.ico");
xld.httpFile('/audio/:file', 		root+"audio/");
xld.httpFile('/css/:file', 			root+"css/");
xld.httpFile('/js/:file', 			root+"js/");
xld.httpFile('/js/vendor/:file', 	root+"js/vendor/");
xld.httpFile('/css/images/:file', 	root+"css/images/");


