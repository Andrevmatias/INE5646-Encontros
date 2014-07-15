var encontrosApp = angular.module('encontrosApp', []);

encontrosApp.controller('ParametrosCtrl', function ($scope, $http) {
	$http.get('mostreInformacoesSistema').success(function(data) {
	    $scope.informacoes = data;
	});
});

encontrosApp.controller('MenuCtrl', function ($scope, $rootScope) {
	$scope.paginas = [
		{nome: "Cadastro", urlTemplate: "assets/templates/cadastro.html"},
		{nome: "Informações", urlTemplate: "assets/templates/parametros.html"},
		{nome: "Pessoas", urlTemplate: "assets/templates/pessoas.html"}
	]
	$rootScope.currentTemplate = $scope.paginas[0].urlTemplate
	$scope.mudarTemplate = function(urlTemplate){
		$rootScope.currentTemplate = urlTemplate;
	};
});

encontrosApp.controller('CadastroCtrl', function ($scope, $http) {
	$scope.cadastre = function(pessoa){
		$http.post('cadastre', pessoa).success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				alert("Pessoa cadastrada")
			}
		});
	}
	$scope.gere = function(pessoa){
		var qtd = prompt("Quantas pessoas devem ser geradas?");
		if(qtd != null){
			if(qtd != "" && !isNaN(qtd)){
				$http.post('gerePessoas', +qtd).success(function(data){
					if(data.cod === "NOK"){
						alert(data.erro);
					} else {
						alert(data.qtdGeradas + " Pessoas geradas com sucesso")
					}
				});
			} else {
				alert("Quantidadade inválida");
			}
		}
	}
});

encontrosApp.controller('PessoasCtrl', function ($scope, $http) {
	$scope.atualize = function(){
		$http.get('listeTodas').success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoas = data.pessoas;
			}
		});
	};
	$scope.atualize();
});