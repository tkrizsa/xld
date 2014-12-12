var vertx = require('vertx');
var xld = require('xld.js');
var Partner = require('partner.js');


xld.log('XLD API Partner started ...');


xld.moduleRegister('partner');
(new Partner()).publish();





xld.template('home');
xld.template('about');
