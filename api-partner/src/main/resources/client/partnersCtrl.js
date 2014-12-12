//@xld-controller : partners

xldApp.__controllerProvider.register('xldCtrlPartners', ['$scope', function ($scope) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('partners', '/api/partners')
	.than(function(resp) {
		console.log(resp);
		console.log($scope.s);
	});
	

	$scope.$on("refresh", function (event, things) {
		things.on('Partner', function() {
			$scope.s.partners.reload()}
		);
	});
	
}]);