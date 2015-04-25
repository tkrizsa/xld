//@xld-controller : expenseKinds

xldApp.__controllerProvider.register('xldCtrlExpenseKinds', ['$scope', function ($scope) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('expensekinds', '/api/expensekinds');
	// .than(function(resp) {
		// console.log(resp);
		// console.log($scope.s);
	// });
	

	$scope.$on("refresh", function (event, things) {
		things.on('ExpenseKind', function() {
			$scope.s.expensekinds.reload()}
		);
	});
	
}]);