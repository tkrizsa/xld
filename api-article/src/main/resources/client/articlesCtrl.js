//@xld-controller : articles

xldApp.__controllerProvider.register('xldCtrlArticles', ['$scope', function ($scope) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('articles', '/api/articles')
	.than(function(resp) {
		console.log(resp);
		console.log($scope.s);
	});
	

	$scope.$on("refresh", function (event, things) {
		things.on('Article', function() {
			$scope.s.articles.reload()}
		);
	});
	
}]);