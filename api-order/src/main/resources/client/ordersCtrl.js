//@xld-controller : orders

xldApp.__controllerProvider.register('xldCtrlOrders', ['$scope', function ($scope) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('orders', '/api/orders')
	.than(function(resp) {
		console.log(resp);
		console.log($scope.s);
	});
	

	$scope.$on("refresh", function (event, things) {
		things.on('Order', function() {
			$scope.s.orders.reload()}
		);
	});
	
}]);