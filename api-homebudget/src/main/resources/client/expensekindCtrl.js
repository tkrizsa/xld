//@xld-controller : expenseKind

xldApp.__controllerProvider.register('xldCtrlExpenseKind', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('expensekind', '/api/expensekinds/'+pg.params.expenseKindId);
	$scope.save = function() {
		$scope.s.expensekind.save(function() {
			$window.history.back();
			$scope.broadcastRefresh('ExpenseKind');
		});
	}
}]);

