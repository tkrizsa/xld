//@xld-controller : article

xldApp.__controllerProvider.register('xldCtrlArticle', ['$scope', '$window', function ($scope, $window) {
	var pg = $scope.page.init($scope);
	$scope.page.getStruct('article', '/api/articles/'+pg.params.articleId);
	$scope.save = function() {
		$scope.s.article.save(function() {
			$window.history.back();
			$scope.broadcastRefresh('Article');
		});
	}
}]);

