//@xld-controller : actor

xldApp.__controllerProvider.register('xldCtrlActor', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('actor', '/api/actors/'+pg.params.actorId);
	$scope.save = function() {
		$scope.s.actor.save(function() {
			$window.history.back();
			$scope.broadcastRefresh('Actor');
		});
	}
}]);

