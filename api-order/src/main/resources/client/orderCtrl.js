//@xld-controller : order

xldApp.__controllerProvider.register('xldCtrlOrder', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	
	$scope.page.getStruct('order', '/api/orders/'+pg.params.orderId+'?_expand=deliveryActor,invoiceActor,details,article');
	
	
	$scope.addDetail = function() {
		var details = $scope.s.order._rows[0].details;
		details.rows.push({});
	
	}
	
	$scope.print = function() {
		console.log($scope.s.order._rows[0].details);
	}
	
	
	$scope.save = function() {
		$scope.s.order.save(function() {
			pg.close();
			$scope.broadcastRefresh('Order');
		});
	}
	
}]);

