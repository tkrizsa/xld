//@xld-controller : expense

xldApp.__controllerProvider.register('xldCtrlExpense', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('expense', '/api/expenses/'+pg.params.expenseId + '?_expand=expenseKind');
	$scope.save = function() {
		$scope.s.expense.save(function() {
			pg.close();
			$scope.broadcastRefresh('Expense');
		});
	}
}]);

