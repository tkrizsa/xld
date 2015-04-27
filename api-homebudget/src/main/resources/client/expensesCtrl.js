//@xld-controller : expenses

xldApp.__controllerProvider.register('xldCtrlExpenses', ['$scope', function ($scope) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('expenses', '/api/expenses?_expand=expenseKind,actor');

	$scope.$on("refresh", function (event, things) {
		things.on('Expense', function() {
			$scope.s.expenses.reload()}
		);
	});
	
}]);