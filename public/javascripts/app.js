var encontrosApp = angular.module('encontrosApp', []);

encontrosApp.controller('ParametrosCtrl', function ($scope, $http) {
	$http.get('mostreInformacoesSistema').success(function(data) {
	    $scope.informacoes = data;
	});
});

encontrosApp.controller('MenuCtrl', function ($scope, $rootScope) {
	$scope.paginas = [
		{nome: "Cadastro", urlTemplate: "assets/templates/cadastro.html"},
		{nome: "Informações", urlTemplate: "assets/templates/parametros.html"}
	]
	$rootScope.currentTemplate = $scope.paginas[0].urlTemplate
	$scope.mudarTemplate = function(urlTemplate){
		$rootScope.currentTemplate = urlTemplate;
	};
});

encontrosApp.controller('CadastroCtrl', function ($scope, $http) {
	
});