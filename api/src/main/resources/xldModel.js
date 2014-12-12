var vertx = require('vertx');
var eb = vertx.eventBus;
var xld = require('xld.js');
var md5 = require('md5.js');

var XldField_id 			= require('xldField_id');
var XldField_stringProp		= require('xldField_stringProp');
var XldField_textProp 		= require('xldField_textProp');
var XldField_enumProp 		= require('xldField_enumProp');
var XldField_reference 		= require('xldField_reference');
var XldField_master 		= require('xldField_master');


var global = this;

var XldModel = function() {

	var thisModel = this;

	var vertx = require('vertx');
	var console = require('vertx/console');
	
	var _tableName 		= 'table';
	var _keyName		= 'id';
	var _keyOffset		= 0;

	this.modelId		= 'model';
	this.fields 		= new Array();
	this.rows 			= new Array();
	this.expands		= {};
	
	/* ================================================== DDL ===================================================== */
	this.tableName = function(x) {
		if (typeof x == 'string')
			_tableName = x;
		return _tableName;
	}
	
	this.fieldAdd = function(fieldName, fieldType, fieldParam1, fieldParam2) {
		var f = false;
		if (typeof fieldName == 'object') {
			f = fieldName;
		} else {
			fc = 'XldField_' + fieldType;
			if (typeof global[fc] != 'function')
				throw "Unknown fieldType '"+ fieldType +"'.";
			f = new global[fc](fieldName, fieldParam1, fieldParam2);
		
		}
		this.fields.push(f);
		if (f.isKey()) {
			_keyName = f.fieldName();
			_keyOffset = this.fields.length-1;
		}
	}
	
	this.getKeyName = function() {
		return _keyName;
	}
	
	this.getKeyValue = function(row) {
	
		return row[_keyOffset];
	}
	
	this.addExpandNN = function(expandModel, modelXFile, modelFile) {
		this.expands[expandModel] = {
				kind 		: 'nn',
				modelXFile	: modelXFile,
				modelFile	: modelFile
		};
	}
	
	/* ================================================== DATA ===================================================== */

	this.getApiRow = function(row, es) {
	
		rrow = {};
		for (var j in this.fields) {
			var fn = this.fields[j].fieldName();
			rrow[fn] = row[j];
		}
		
		if (es) {
			for (var i in es) {
				var emi = es[i];
				if (!this.expands[emi])
					continue;
				var exp = this.expands[emi];
				if (row[exp.modelId]) {
					rrow[emi] = row[emi];
				} else {
					rrow[emi] = [];
				}
			}
		}
		
		this.addLink(rrow);
		
		return rrow;
	
	}
	
	// should rename to getApiData
	this.get = function(exps) {
		var es = false;
		if (exps)
			es = exps.split(',');
	
		var res = [];
		for (var i in this.rows) {
			res.push(this.getApiRow(this.rows[i], es));
		}
		
		var ret = {rows : res};

		if (es) {
			var templates = {};
			for (var i in es) {
				var emi = es[i];
				if (!this.expands[emi])
					continue;
				var exp = this.expands[emi];
				xld.log('----:----', exp.modelClass);
				var exModel = new exp.modelClass();
				exModel.addEmptyRow();
				templates[emi] = exModel.getApiRow(exModel.rows[0]);
			}
			ret.templates = templates;
		} 
		return ret;
	}
	
	this.addEmptyRow = function() {
		var row = [];
		for (var i in this.fields) {
			row.push(null);
		}
		this.rows.push(row);
		return row;
	}
	
	this.loadSql = function(query, func) {
		eb.send('xld-sql-persist', { "action" : "raw",  "command" : query}, function(res) 	{
			if (res.status == 'ok') {
				var fm = {}; //fieldMap
				xld.log("-----------------------------------------------");
				srow = '';
				for (var i in res.fields) {
					srow += res.fields[i] + ' | ';
					for (var j in thisModel.fields) {
						var f = thisModel.fields[j];
						if (res.fields[i] == f.fieldName())
							fm[j] = i;
					}
				}
				
				xld.log(srow, "-----------------------------------------------");
				for (var i in res.results) {
					var srow = '';
					for (var j in res.results[i]) {
						srow += res.results[i][j] + ' | ';
					}
					xld.log(srow);
				}
				xld.log("-----------------------------------------------");
				
				for (var i in res.results) {
					var newrow = new Array();
					for (var j in  thisModel.fields ) {
						if (fm[j]) {
							newrow.push(res.results[i][fm[j]]);
						} else {
							newrow.push(null);
						}
					}
					thisModel.rows.push(newrow);
				}
				
				if (typeof func == 'function') {
					func();
				}
				
			} else {
				xld.log("SQL ERROR : " + res.message);
				if (typeof func == 'function') {
					func(res.message);
				}
			}
		});
	}
	
	this.clear = function() {
		this.rows = [];
	}
	
	this.load = function(id, func) {
		this.clear();
		this.loadSql("SELECT * FROM `" + this.tableName() + "` WHERE `" + _keyName + "` = '" + id + "'", function(err) {
			if (typeof func == 'function') func(err);
		});
	}

	this.loadList = function(func) {
		this.clear();
		this.loadSql("SELECT * FROM `"+ this.tableName() +"` ", function(err) {
			if (typeof func == 'function') func(err);
		});
	}
	
	
	
	this.loadPost = function(body, exps) {
		//body = JSON.parse(body);
			
		this.rows = [];
		for(var i in body.rows) {
			this.loadPostRow(body.rows[i], exps);
		}
		
	}
	
	this.loadPostRow = function(brow, exps) {
		var es = false;
		if (exps)
			es = exps.split(',');		
	
		var row = {};
		for (var j in this.fields) {
			var fn = this.fields[j].fieldName();
			row[j] = brow[fn];
		}
		
		if (es) {
			for (var i in es) {
				var emi = es[i];
				if (!this.expands[emi])
					continue;
				var exp = this.expands[emi];
				
				if (brow[emi]) {
					var o = new exp.modelClass();
					o.loadPost({rows:brow[emi]}, exps);
					row[emi] = o; 
				} else {
					row[emi] = [];
					
				}
			}
		}
		
		this.rows.push(row);
	}
	
	this.saveSql = function(func) {
		var rc = 0;
		var serr;
		for (var i in this.rows) {
			rc++;
			this.saveSqlRow(this.rows[i], function(err) {
				if (err) {
					if (typeof serr == 'undefined')
						serr = '';
					serr += '\r\n' + err;
				}
				rc--;
				if (typeof func == 'function' && rc == 0) {
					func(serr);
				}
			});
		}
	}
	
	this.saveSqlRow = function(row, func) {
		var sql = ""
			+ "UPDATE `" + this.tableName() + "`\r\n"
			+ "SET\r\n";
		var kv = '';
		var sep = '';
		for (var i in this.fields) {
			var fn = this.fields[i].fieldName();
			if (fn == _keyName)	{
				kv = row[i];
				continue;
			}
				
			var val = row[i];
			if (val === null) {
				val = 'NULL';
			} else {
				val = "'" + val + "'"
			}
			sql += sep + "`"+ fn + "` = " + val + "\r\n";
			sep = ', ';
			
		}
		
		
		if (kv == null) {
			this.saveSqlRowInsert(row, func);
			return;
		
		}
		
		sql += "WHERE `" + _keyName + "` = '" + kv + "'";
		console.log(sql);
		eb.send('xld-sql-persist', { "action" : "raw",  "command" : sql}, function(res) 	{
			if (res.status == 'ok') {
				if (typeof func == 'function') 
					func();
			} else {
				xld.log("SQL ERROR : " + res.message);
				if (typeof func == 'function') 
					func(res.message);
			}
		});
	
	}
	
	this.saveSqlRowInsert = function(row, func) {
		var sql = ""
			+ "INSERT INTO `" + this.tableName() + "` (\r\n";
		var sep = '';
		var sqlvals = '';
		for (var i in this.fields) {
			var fn = this.fields[i].fieldName();
			if (fn == _keyName)	{
				continue;
			}
			
			var val = row[i];
			if (val === null) {
				val = 'NULL';
			} else {
				val = "'" + val + "'"
			}
			
				
			sql += sep + "`"+ fn + "`";
			sqlvals += sep + val;
			sep = ', ';
			
			
		}
		sql += "\r\n ) \r\n VALUES (\r\n" + sqlvals + "\r\n)";
		
		console.log(sql);
		eb.send('xld-sql-persist', { "action" : "raw",  "command" : sql}, function(res) 	{
			if (res.status == 'ok') {
				if (typeof func == 'function') 
					func();
			} else {
				xld.log("SQL ERROR : " + res.message);
				if (typeof func == 'function') 
					func(res.message);
			}
		});
	
	}
	
	/* ============================================= expand ==================================== */
	this.expandInit = function(exps) {
		var es = exps.split(',');
		for (var esi in es) {
			
			if (!this.expands[es[esi]]) 
				continue;

			var exp = this.expands[es[esi]];
			if (exp.kind == 'nn') {
				exp.modelClass = require('./model/' + exp.modelFile + '.js');
				exp.model = new exp.modelClass();
				exp.modelXClass = require('./model/' + exp.modelXFile + '.js');
				exp.modelX = new exp.modelXClass();
			}
		}
	}
	
	this.expandLoad = function(exps, func) {
		var es = exps.split(',');
		
		var loadNow = [];
		for (var esi in es) {
			if (!this.expands[es[esi]]) 
				continue;
				
			var exp = this.expands[es[esi]];
			loadNow.push(exp);
		}
		
		if (loadNow.length == 0) {
			func();
			return;
		}
		var loadNowI = 0;
		
		var loadFunc = function(i) {
			var exp = loadNow[i];
			if (exp.kind == 'nn') {
				exp.model.loadDetailNN(thisModel, exp.modelX, function(err) {
					if (err) {
						func(err);
						return;
					} 
					i++;
					if (i>=loadNow.length) {
						func();
					} else {
						loadFunc(i);
					}
				});
			
			}
		}
		loadFunc(loadNowI);
		
	}
	
	this.loadDetailNN  = function(master, modelX, func) {
		this.clear();
		
		var xKeyName0 = '';
		var xKeyName1 = '';
		xld.log('------ check keynames ------');
		for (var i in modelX.fields) {
			var f = modelX.fields[i];
			if (f instanceof XldField_master) {
				xld.log('bent', f.refModelId);
				if (f.refModelId == this.modelId)
					xKeyName0 = f.fieldName();
				if (f.refModelId == master.modelId)
					xKeyName1 = f.fieldName();
				
			}
		}
		
		xld.log('------- keyvalues ---------');
		var keyvalues = [];
		for (var i in master.rows) {
			var val = master.getKeyValue(master.rows[i]);
			xld.log('val ', val, i, master.rows[i]);
			keyvalues .push(val);
		
		}
		
		var query = '';
		query += "SELECT t0.* FROM `" + this.tableName() + "` t0 \r\n";
		query += "INNER JOIN `" + modelX.tableName() + "` t1 ON t1.`" + xKeyName0 + "` = t0.`" + this.getKeyName() + "` \r\n";
		query += "WHERE t1.`" + xKeyName1 + "` IN (" + keyvalues.join(', ') + ") \r\n";
		query += "";
		xld.log(query);
		this.loadSql(query, function(err) {
			if (typeof func == 'function') func(err);
		});
	}
	
	
	/* ================================================== CONTROLLER functions ===================================================== */
	
	
	
	
	this.publish = function(name, readonly) {
		if (!name)
			name = this.modelId;
		this.install();
		this.publishList(name);
		this.publishElem(name, readonly);
	}
	
	this.publishList = function(name) {
		xld.api('/api/'+name+'s', function(req, replier) {
			var p = new thisModel.constructor();
			p.loadList(function(err) {
				if (err) {
					replier(err);
				} else {
					replier(p.get());
				}
			});
		});
		
		xld.template(name + 's');
	}
	
	this.publishElem = function(name, readonly) {

		xld.api('/api/'+name+'s/:id', function(req, replier) {
		
			var exps = false;
			if (req.params._expand)
				exps = req.params._expand;
		
			var p = new thisModel.constructor();
			if (exps)
				p.expandInit(exps);
				
			if (req.params.id == 'new') {
				p.addEmptyRow();
				replier(p.get(exps));
			} else {
				p.load(req.params.id, function(err) {
					if (err) {
						replier(err);
					} else {
						if (exps) {
							p.expandLoad(exps, function(err) {
								if (err) {
									replier(err);
								} else {
									replier(p.get(exps));
								}
							});
						
						} else {
							// nothing to expand
							replier(p.get());
						
						}
					}
				});
			}
		});

		if (!readonly) {
			xld.apiPost('/api/'+name+'s/:id', function(req, replier) {

				var p = new thisModel.constructor();

				var exps = false;
				if (req.params._expand)
					exps = req.params._expand;			
				if (exps)
					p.expandInit(exps);
			
				p.loadPost(JSON.parse(req.body), exps);
				p.saveSql(function(err) {
					if (err) {
						replier(null, err);
					} else {
						replier(p.get(), false, name);
					}
				});
			});
		}

		xld.template(name, name + 's/:id');
	}
	
	
	
	/* ================================================== INSTALL ===================================================== */
	this.install = function() {
		//xld.log('------------------ INSTALL ' + this.modelId + ' ---------------------------');
		var vst = this.tableName() + '#';
		for (var i in this.fields)
			vst += this.fields[i].toString() + '|';
		var vcode = md5(vst);
		//xld.log(vcode);
		
		var query = "SELECT modelVersion FROM xld_sql_install WHERE modelId = '"+thisModel.modelId+"' ORDER BY installId ASC";
		eb.send('xld-sql-persist', { "action" : "raw",  "command" : query}, function(res) 	{
			if (res.status != 'ok') {
				xld.log("SQL ERROR : " + res.message);
				return;
			}
			var curVer = '';
			for (var i in res.results) {
				var row = res.results[i];
				curVer = row[0];
			}
			if (curVer != vcode) {
				xld.log('----------------- INSTALLING MODEL ' + thisModel.modelId + ' --------------------------');
				xld.log('OLD:'+curVer, 'NEW:'+vcode);
			
				thisModel.installLoad(function (inst) {
					var inAct = -1;
					for (var i = inst.length-1; i>=0; i--) {
						if (inst[i].modelVersion == vcode) {
							inAct = i;
							break;
						}
					}
					if (inAct<0) {
						xld.log('INSTALL REQUIRED, BUT MISSING!', 'model: '+ thisModel.modelId, 'version : ' + vcode);
						return;
					}
					var inLast = -2;
					if (curVer == '') {
						inLast = -1;
					} else {
						for (var i = inst.length-1; i>=0; i--) {
							if (inst[i].modelVersion == curVer) {
								inLast = i;
								break;
							}
						}
					}
					if (inLast<-1) {
						xld.log('INSTALL REQUIRED, BUT LAST INSTALLED VERSION MISSING!', 'model: '+ thisModel.modelId, 'version : ' + curVer);
						return;
					}
					
					xld.log('current, last', inAct, inLast);
					var queries = [];
					for (var i = inLast+1; i<=inAct; i++) {
						xld.log("INSTALLING VERSION : " + inst[i].modelVersion);
						
						queries.push(inst[i].sqlScript);
						
						var query = "INSERT INTO xld_sql_install (modelId, modelVersion, versionHint, sqlScript) ";
						query += " VALUES('"+thisModel.escape(thisModel.modelId)+"', '"+thisModel.escape(inst[i].modelVersion)+"', '"+thisModel.escape(inst[i].versionHint)+"', '"+thisModel.escape(inst[i].sqlScript)+"')";
					
						queries.push(query);
					}
					
					
						
						
					thisModel.dbUpdate(queries, false, function(err) {
						if (!err) {
							xld.log('INSTALL OK.');
						}
					});
					
				});
					
			}
			
		});
		
	
	}
	
	
	this.installLoad = function(func) {
	

		var installScripts = [];
		var fileName = './install/'+this.modelId+'.sql';
		vertx.fileSystem.readFile(fileName, function(err, file) {
			if (err) {
				xld.log('Error reading '+fileName, err);
				return;
			}
			var newScript = {
				modelVersion : false,
				versionHint : '',
				sqlScript : ''
			};
			
			var lines = file.toString().replace(/\r/gm, "").split("\n");
			for (var l in lines) {
				var line = lines[l];
				
				if (line.indexOf('--@xld') === 0) {
					var keyval = line.substr(7).trim();
					var p = keyval.indexOf(':');
					var key, val;
					if (p>=0) {
						key = keyval.substr(0,p).trim();
						val = keyval.substr(p+1).trim();
					} else {
						key = keyval;
						val = true;
					}
					if (key == 'modelVersion') {
						
						if (newScript.modelVersion) {
							installScripts.push(newScript);
						}
						newScript = {
							modelVersion : val,
							versionHint : '',
							sqlScript : ''
						}
					} else {
						newScript[key] = val;
					}
				} else {
					newScript.sqlScript += line + "\r\n";
				}
			
			}
			if (newScript.modelVersion) {
				installScripts.push(newScript);
			}
			
			func(installScripts);
		});
	}

}


XldModel.prototype.addLink = function(row) {
	var kv = row[this.getKeyName()];
	kv = kv == null ? 'new' : kv;
	row.self = {
		href : '/api/'+this.modelId+'/' + kv
	}
	row.gui = {
		href : '/'+this.modelId+'s/' + kv
	}
}

// ============================================= TOOLS =========================================

XldModel.prototype.escape = function(str) {
    return str.replace(/[\0\x08\x09\x1a\n\r"'\\\%]/g, function (char) {
        switch (char) {
            case "\0":
                return "\\0";
            case "\x08":
                return "\\b";
            case "\x09":
                return "\\t";
            case "\x1a":
                return "\\z";
            case "\n":
                return "\\n";
            case "\r":
                return "\\r";
            case "\"":
            case "'":
            case "\\":
            case "%":
                return "\\"+char; // prepends a backslash to backslash, percent,
                                  // and double/single quotes
        }
    });
}

XldModel.prototype.dbUpdate = function(querys, inTrans, func) {
	if (typeof querys == 'string')
		querys = [querys];
		
	if (typeof inTrans == 'function') {
		func = inTrans;
		inTrans = true;
	}

	var actions = [];
	if (inTrans) {
		actions.push({action : 'raw', command : 'BEGIN'});
	}
	for (var i in querys) {
		actions.push({
			action : 'raw',
			command : querys[i]
		});
	}
	if (inTrans) {
		actions.push({action : 'raw', command : 'COMMIT'});
	}
	
	
	var actionIx = 0;
	var runAction = function(action) {
		xld.log('==== RUNACTION ====',  action.action=='raw'?action.command:action);
		eb.send('xld-sql-persist', action, function(res) 	{
			if (res.status != 'ok') {
				
				if (action.action == 'raw' && action.command)
					vertx.fileSystem.writeFile('./../../sql/_last_failed.sql', action.command);
				//vertx.fileSystem.writeFile('./sql/_last_failed.sql', action.command);
				
				var xmessage = res.message;
				xld.log("SQL ERROR : " + res.message);
				if (inTrans) {
					eb.send('xld-sql-persist', {action : 'raw', command : 'ROLLBACK'}, function(res) 	{
						if (res.status != 'ok') {
							xld.log('ROLLBACK FAILED!', res.message);
						} else {
							xld.log('ROLLBACK OK.');
						}
						func(xmessage);
					});
				}
			} else {
				xld.log('OK.');
				if (actionIx<actions.length-1) {
					actionIx++;
					runAction(actions[actionIx]);
				} else {
					func();
				}
			}
		});
	}
	runAction(actions[actionIx]);
}



/*Elem.prototype.extend = function(obj) {
	for(var e in Elem) {
		if (Elem.hasOwnProperty(e)) {
			obj[e] = Elem[e];
		
		}
	}
}*/


module.exports = XldModel;