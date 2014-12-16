//@xld-controller : order

xldApp.__controllerProvider.register('xldCtrlOrder', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('order', '/api/orders/'+pg.params.orderId);
	$scope.save = function() {
		$scope.s.order.save(function() {
			$window.history.back();
			$scope.broadcastRefresh('Order');
		});
	}
	
}]);

