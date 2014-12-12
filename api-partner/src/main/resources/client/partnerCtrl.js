//@xld-controller : partner

xldApp.__controllerProvider.register('xldCtrlPartner', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('partner', '/api/partners/'+pg.params.partnerId);
	$scope.save = function() {
		$scope.s.partner.save(function() {
			$window.history.back();
			$scope.broadcastRefresh('Partner');
		});
	}
}]);

