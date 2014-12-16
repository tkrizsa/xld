
$(function() {
	// $.get('/api/partner/1', function(resp) {
		// $('body').append(resp);
	// });
	
	// 
	$('#xld-nav-main li:has(ul)' ).doubleTapToGo();	
	//$("#xld-nav-main1").mmenu();
	//$("#xld-nav-main1").trigger("open.mm");
	
	$("#xld-nav-main ul ul a").click(function() {
		var $ul = $(this).closest('ul');
		$ul.addClass('hideonclick');
		setTimeout(function() {
			$ul.removeClass('hideonclick');
		},10);
	});
	$("#xld-nav-main > ul > li > a").click(function() {
		var $ul = $(this).closest('li').find('ul');
		$ul.addClass('hideonclick');
		setTimeout(function() {
			$ul.removeClass('hideonclick');
		},500);
	});
});



// ======================= config ANGULAR ==============================
xldApp = angular.module('xldApp', []);


xldApp.config(function ($controllerProvider, $sceProvider, $locationProvider) {
	$sceProvider.enabled(false);
	xldApp.__controllerProvider = $controllerProvider;
	
	$locationProvider.html5Mode(true);
	$locationProvider.hashPrefix('!');
	
	//xldProvider.setSomeConfig(true);	
});

// ======================= xld PAGE Framework ==============================
var xld = xld || {};
xld.Page = function(mainScope, ix, urlInfo) {
	this.mainScope 	= mainScope;
	this.scope		= false;
	this.ix 		= ix;

	this.urlInfo	= urlInfo;
	this.title 		= urlInfo.url;
	this.template 	= urlInfo.template;
	this.params		= urlInfo.params ? urlInfo.params : {};
	this.url		= urlInfo.url;
	
	this.colsOpt 	= 1;
	this.colsMin	= 1;
	this.colsMax	= 1;
	this.widthSet	= false;
	
	this.structs	= [];
	
	var thisPage = this;
	
	this.reParseUrl = function() {
		this.urlInfo = this.mainScope.parsePageUrl(this.url);
		this.title 		= this.urlInfo.url;
		this.template 	= this.urlInfo.template;
		this.params		= this.urlInfo.params ? this.urlInfo.params : {};
		this.url		= this.urlInfo.url;
	}
	
	
	this.templateLoaded = function() {
		this.mainScope.setScroll();
	}
	
	this.init = function(scope) {
		// scope must call it as first in controller function
		this.scope = scope;
		scope.s = this.structs;
		return this;
	}
	
	this.getStruct = function(alias, url) {
		// create a new structure, saves in structures array and returns the promise object
		var struct = this.structs[alias] = xld.getStruct(this, url, this.mainScope);
		return struct;
		
	}
	
	this.getStructOne = function(alias, oneAlias, url) {
		// create a new structure, saves in structures array and returns the promise object
		var struct = this.structs[alias] = xld.getStruct(this, url, this.mainScope);
		struct.than(function() {
			thisPage.structs[oneAlias] = struct._rows[0];
		});
		return struct;
		
	}
	
	this.goForward = function($event, href, opener) {
		$($event.target).closest('.selectable').parent().find('.selected').removeClass('selected');
		$($event.target).closest('.selectable').addClass('selected');

		var _bs = this.scope.getBrowserArray(this); // lastPage = this, close every page after this

		var n = {
			seg : 'main',
			pos : _bs.pgs.length,
			url : href
		}
		if (opener)
			n.opener = opener;
		_bs.pgs.push(n);
		href = xld.addParameter(href, '_bs', this.scope.getBrowserEncoded(_bs));

		this.scope.setUrl(href);
	}

	this.goForwardSub = function($event, href, opener) {
		$($event.target).closest('.selectable').parent().find('.selected').removeClass('selected');
		$($event.target).closest('.selectable').addClass('selected');

		var _bs = this.scope.getBrowserArray(this); // lastPage = this, close every page after this
		var n = {
			seg : 'main',
			pos : _bs.pgs.length,
			url : href
		}
		if (opener)
			n.opener = opener;
		_bs.pgs.push(n);
		newHref = xld.addParameter(this.url, '_bs', this.scope.getBrowserEncoded(_bs));
		this.scope.setUrl(newHref);
	}
	
	this.close = function() {
		var lastPage = this.getPreviousPage();
		if (lastPage) {
			var _bs = this.scope.getBrowserArray(lastPage);
			var url = _bs.pgs[_bs.pgs.length-1].url;
			url = xld.addParameter(url, '_bs', this.scope.getBrowserEncoded(_bs));
			this.scope.setUrl(url);
		} else {
			alert("close and not last page?");
		}
	};
	
	
	this.goDefault = function($event, struct) {
		$($event.target).closest('.selectable').parent().find('.selected').removeClass('selected');
		$($event.target).closest('.selectable').addClass('selected');
		if (this.opener) {
			var lastPage = this.getPreviousPage();
			lastPage.subpageResult(this.opener, struct);
			this.close();
		} else {
			this.goForward($event, struct.gui.href);
		}
	}
	
	this.getPreviousPage = function() {
		var ix = -1;
		for (var i in this.mainScope.pages) {
			if (!this.mainScope.pages.hasOwnProperty(i))
				continue;
			if (i<this.ix && i> ix)
				ix = i;
		}
		return this.mainScope.pages[ix];
	}

	this.subpageResult = function(opener, struct) {
		this.scope.$broadcast("subpageResult", {
			key : opener,
			value : struct
		});
	}
	
}

/** ============================================== APP CONTROLLER ==================================================== **/
xldApp.controller('xldMain', ['$scope', '$location', '$timeout', '$templateCache', '$window', function ($scope, $location, $timeout, $templateCache, $window) {

	$scope.MAX_PAGE_IX = -1;
	$scope.dims 		= {};
	$scope.pages 		= {};
	$scope.urlParsers 	= [];
	$scope.pendingUrls 	= [];
	
	xld.mainScope = $scope;
	
	$scope.setUrl = function(url) {
		$location.url(url);
	}
	
	$scope.$on('$locationChangeStart', function(e, newUrl, oldUrl){
		// console.log('$locationChangeStart');
		// console.log($location.path());
		// console.log('oldUrl : ' + oldUrl);
		// console.log('newUrl : ' + newUrl);
	});
	
	$scope.$on('$locationChangeSuccess', function(){
		console.log('$locationChangeSuccess');
		console.log($location.path());
		$scope.mainUrl = $location.path();
		var search = $location.search();
		var bs = {pgs : []};
		if (search._bs) {
			bs = $.base64.decode(search._bs);
			bs = JSON.parse(bs);
		}
		
		if (bs.pgs.length<=0) {
			bs.pgs.push({
					seg : 'main',
					pos : bs.pgs.length,
					url : $scope.mainUrl
			});
		}
		
		
		console.log(bs);
		// $scope.clearPages();
	
		// delete the pages not fitting to url
		var j = 0;
		var last_ok_bs = -1;
		var ok = true;
		for( var i in $scope.pages) {
			var a = $scope.pages[i];
			if (ok && bs.pgs[j]) {
				var b = bs.pgs[j];
				if (a.url == b.url) {
					last_ok_bs = j;
					j++;
					continue;
				}
			}

			ok = false;
			delete $scope.pages[i];
		}
		
		// load new pages
		for (var i in bs.pgs) {
		
			if (i<=last_ok_bs)
				continue;
				
			var p = bs.pgs[i];
			
			var ix = ++$scope.MAX_PAGE_IX;
			if (p.url == '/' || p.url == '')
				p.url = '/home';
			var urlInfo = $scope.parsePageUrl(p.url, true);
			$scope.pages[ix] = new xld.Page($scope, ix, urlInfo);
			$scope.pages[ix].opener = p.opener;
		}
		
		$scope.askPendingUrls();
		
		$timeout(function() {
			$scope.setScroll(last_ok_bs<0);
		},1);
	});
	
	$scope.parsePageUrl = function(url, tryLoadParser) {
		var urlx = url;
		if (urlx.substr(0,1)=='/')
			urlx = urlx.substr(1);
		var urla = urlx.split('/');
		for (var i in $scope.urlParsers) {
			var parser = $scope.urlParsers[i];
			var info = parser(url, urla);
			if (info) {
				return info;
			}
		}
		
		if (tryLoadParser)
			$scope.pendingUrls.push(url);
		return {
			url : url,
			template : false,
			params : false,
			pending : true
		}
		
		

	}
	
	$scope.askPendingUrls = function() {
		if ($scope.pendingUrls.length <= 0) 
			return;
		$.ajax({
			url : '/parseUrls', 
			data : {urls : JSON.stringify($scope.pendingUrls)}, 
			success : function(resp) {
				var hasNewParser = false;
				for (var i in resp) {
					if (!resp.hasOwnProperty(i))
						continue;
					var code = resp[i];
					if (code.kind == 'parser' || code.kind == 'controller')  {
						//eval(code.body);
						var s = document.createElement("script");
						s.type = "text/javascript";
						s.innerHTML = code.body;
						$("head").append(s);						
						
						hasNewParser = true;
					}
					if (code.kind == 'template') {
						$templateCache.put(code.templateName, code.body);
					}
				}
				if (hasNewParser) {
					for (var ix in $scope.pages) {
						var page = $scope.pages[ix];
						if ( page.urlInfo.pending) {
							page.reParseUrl();
						}
					
					}
					$scope.$apply();
				}
				
			},
			error : function(resp) {
				new jBox('Notice', {
					content : resp.responseText,
					attributes: {x: 'left', y: 'bottom'},
					position: 	{x: 50,	y: 5},
					color : 'red',
					theme: 'NoticeBorder'
				});
			}
		});
		
		$scope.pendingUrls = [];
	}

	
	$scope.clearPages = function() {
		for (var i in $scope.pages) {
			if ($scope.pages.hasOwnProperty(i)) {
				//$('#xld-page-' + $scope.pages[i].ix).hide();
				delete $scope.pages[i];
			}
		}
		$('#xld-main-scroll').css({'left' : '0px'  });			
		$scope.MAX_PAGE_IX = -1;
	};
	
	$scope.resetDims = function() {
		this.dims.mainWidthPx 		= $('#xld-main').width();
		this.dims.emSize 			= parseFloat($("body").css("font-size"));
		this.dims.mainWidthEm 		= this.dims.mainWidthPx / this.dims.emSize;
		this.dims.mainColCount 		= Math.floor(this.dims.mainWidthEm / 20);
		this.dims.colWidthPx 		= Math.floor(this.dims.mainWidthPx / this.dims.mainColCount);
	}
	
	$scope.resetDims();
	
	$scope.getMaxWidth = function() {
		return $scope.dims.mainWidthPx + 'px';
	}
	
	$scope.setScroll = function(noAnimate) {
		if ($scope.pages.length<=0) {
			$('#xld-main-scroll').css({'left' : '0px'  });
			return;
		}
	
		$scope.resetDims();

		
		var ps = new Array();
		for (var i in $scope.pages) {
			ps.push($scope.pages[i]);
		}
		ps = ps.reverse();
		var colsLeft = $scope.dims.mainColCount;
		for (var i in ps) {
			var p = ps[i];
			if (colsLeft >= p.colsOpt)
				p.colsAkt = p.colsOpt;
			else  
				p.colsAkt = colsLeft;
			if (p.colsAkt <= 0)
				p.colsAkt = 1;
			colsLeft -= p.colsAkt;
		}
		/*if (colsLeft > 0) {
			ps[0].colsAkt += colsLeft;
		}*/
	
		for (var i in ps) {
			var xw = (ps[i].colsAkt * $scope.dims.colWidthPx) + 'px';
			var $page = $('#xld-page-' + ps[i].ix);
			$page.stop(true);
			$page[noAnimate?'css':'animate']({'width' : xw});
		}
	
		
		var xl = 0;
		if (colsLeft < 0)
			xl = colsLeft * $scope.dims.colWidthPx;
		else if (colsLeft > 0) {
			xl = (colsLeft * $scope.dims.colWidthPx) / 2;
		}
			
		console.log('scroll to '  + xl);
		var $scroll = $('#xld-main-scroll');
		$scroll.stop(true);
		$scroll[noAnimate?'css':'animate']({'left' : xl + 'px'  });
	}
	
	$scope.getBrowserArray = function(lastPage) {
		var bs = {};
		bs.pgs = new Array();
		var i = 0;
		for (var ix in $scope.pages) {
			if (!$scope.pages.hasOwnProperty(ix)) 
				continue;
			var page = $scope.pages[ix];
			bs.pgs.push({
				seg : 'main',
				pos : i,
				url : page.url
			});
			i++;
			if (lastPage && lastPage == page) 
				break;
		}
		return bs;
	}
	
	
	$scope.getBrowserEncoded = function(bs) {
		return $.base64.encode(JSON.stringify(bs));;
	}
	
	
	$scope.goForward = function(href,  $event) {
		console.log($event);
		
		$($event.target).closest('.selectable').parent().find('.selected').removeClass('selected');
		$($event.target).closest('.selectable').addClass('selected');
	
		var newHref = href;
		var _bs = $scope.getBrowserArray();
		if (_bs.pgs.length>0) {
			_bs.pgs.push({
				seg : 'main',
				pos : _bs.pgs.length,
				url : newHref
			});
			newHref = xld.addParameter(href, '_bs', getBrowserEncoded(_bs));
		}
		$location.url(newHref);
		
	}
	
		/* ============================= REFRESH ============================== */

		// refresh('CompanyAddresses');
		// refresh('CompanyAddresses', {obj : {}});
		// refresh(['CompanyAddresses' : {}, ...]);

		xld.Refresh = function (p1, p2) {
			this.things = {};

			if (typeof p1 == 'string') {
				if (typeof p2 == 'object') {
					this.things[p1] = p2;
				} else {
					this.things[p1] = {};
				}
			} else if (typeof p1 == 'object') {
				for (var i in p1) {
					this.things[i] = p1[i];
				}
			}
		}

		xld.Refresh.prototype.on = function (thing, p2, p3, p4) {
			if (typeof p2 == 'function') {
				if (this.things[thing]) {
					p2(this.things[thing]);
				}
				return;
			}
			if (typeof p4 == 'function') {
				if (this.things[thing]) {
					var tobj = this.things[thing];
					if (typeof tobj[p2] != 'undefined' && tobj[p2] == p3) {
						p4(this.things[thing]);
					}
				}
				return;
			}
		}

		$scope.broadcastRefresh = function (p1, p2) {
			var things = false;
			if (typeof p1 == 'object ' && p1 instanceof xld.Refresh) {
				things = p1;
			} else {
				things = new xld.Refresh(p1, p2);
			}
			$scope.$broadcast('refresh', things);
		}


	$scope.back = function() {
		$window.history.back();

	}

}]);


	/* =================================================== DIRECTIVES ======================================== */

	// Handles browser window resizing ... sets windowArea size
	xldApp.directive('xldWindowResize', function ($window) {
		return function (scope, element) {
			var w = angular.element($window);
			
			w.bind('resize', function () {
				var head = $('#xld-nav-main');			
				var main = $('#xld-main');
				main.css({height : (w.height() - head.height())+'px'});
				if (!scope.$$phase)
					scope.$apply(function() {
						scope.setScroll();
					});
					
			});
			w.trigger('resize');
		}
	});
	
	xldApp.directive('xldScroll', function () {
		return function (scope, element) {
			/*element.enscroll({
				verticalTrackClass: 'xld-scroll-track',
				verticalHandleClass: 'xld-scroll-handle',
				showOnHover: true,
				minScrollbarLength: 28
			});	*/
		
		}
	});
	
	/*xldApp.directive('xldForward', function () {
		return function (scope, element) {
			element.click(function(e) {
				var _bs = scope.getBrowserArray();
				var oldHref = element.attr('href');
				var newHref = oldHref;
				if (_bs != '')
					newHref = xld.addParameter(oldHref, '_bs', _bs);
				element.attr('href', newHref);
			});	
		
		}
	});*/
	
	xldApp.directive('xldPage', function () {
		return function (scope, element, attrs) {
			console.log('===PAGE===');
			var a = attrs['xldPage'].split(',');
			scope.page.colsOpt = parseInt(a[0]);
			scope.page.colsMin = parseInt(a[1]);
			scope.page.colsMax = parseInt(a[2]);
			scope.setScroll();
		}
	});
	
	xldApp.filter('pageSort', function () {
		return function (arrInput) {

			var s = new Array();
			for (var w in arrInput) {
				if (!arrInput.hasOwnProperty(w))
					continue;
				s.push(arrInput[w]);
			}

			var arr = s.sort(function (a, b) {
				if (a.ix > b.ix) { return 1; }
				if (a.ix < b.ix) { return -1; }
				return 0;
			});
			return arr;
		}
	});




	xldApp.directive('xldReferenceActorActor', function () {
		return {
			restrict: 'EA',
			require: 'ngModel',
			scope: {	
				ngModel: '='
			},
			link: function($scope, elem, attrs, ngModel) {
				elem.bind('click', function(e) {
					$scope.$apply(function() {
						$scope.$parent.page.goForwardSub(e, "/actors", "actorId");
					});
				});
				
				$scope.$on('subpageResult', function(e, sr) {
					if (sr.key != 'actorId')
						return;
					ngModel.$setViewValue(sr.value.actorName);
					ngModel.$render();
				});
				
			
			}
		}
	});





// ======================= resume ANGULAR BOOTSTRAP ==============================
angular.element().ready(function () {
	angular.resumeBootstrap(['xldApp']);
});



