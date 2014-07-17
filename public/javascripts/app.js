var encontrosApp = angular.module('encontrosApp', []);

encontrosApp.controller('ParametrosCtrl', function ($scope, $http) {
	$http.get('mostreInformacoesSistema').success(function(data) {
	    $scope.informacoes = data;
	});
});

encontrosApp.controller('MenuCtrl', function ($scope, $rootScope) {
	$scope.paginas = [
		{nome: "Home", urlTemplate: "assets/templates/home.html"},
		{nome: "Cadastro", urlTemplate: "assets/templates/cadastro.html"},
		{nome: "Pessoa", urlTemplate: "assets/templates/pessoa.html"},
		{nome: "Pessoas", urlTemplate: "assets/templates/pessoas.html"},
		{nome: "Desejadas", urlTemplate: "assets/templates/desejadas.html"},
		{nome: "Pesquisa", urlTemplate: "assets/templates/pesquisa.html"},
		{nome: "Informações", urlTemplate: "assets/templates/parametros.html"}
	]
	$rootScope.currentTemplate = $scope.paginas[0].urlTemplate
	$scope.mudarTemplate = function(urlTemplate){
		$rootScope.currentTemplate = urlTemplate;
	};
});

encontrosApp.controller('CadastroCtrl', function ($scope, $http) {
	$scope.pessoa = {};
	$scope.cadastre = function(pessoa){
		$http.post('cadastre', pessoa).success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoa = {};
				alert("Pessoa cadastrada");
			}
		});
	}
	$scope.gere = function(){
		var qtd = prompt("Quantas pessoas devem ser geradas?");
		if(qtd != null){
			$scope.gerando = true;
			$http.post('gerePessoas', +qtd).success(function(data){
				if(data.cod === "NOK"){
					alert(data.erro);
				} else {
					alert(data.qtdGeradas + " Pessoas geradas com sucesso")
				}
				$scope.gerando = false;
			});
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

encontrosApp.controller('DesejadasCtrl', function ($scope, $http) {
	$scope.atualize = function(){
		$http.get('desejadasAoMenosUmaVez').success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoasDesejadas = data.pessoas;
			}
		});
	};
	$scope.atualize();
});

encontrosApp.controller('PessoaCtrl', function ($scope, $http) {
	$scope.leia = function(cpf){
		$http.get('leiaPessoa', { params: { "cpf": cpf }}).success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoa = data.pessoa;
			}
		});
	};
});

encontrosApp.controller('EstatisticasCtrl', function ($scope, $http) {
	$scope.atualize = function(){
		$http.get('mostreEstatisticas').success(function(data) {
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
		    	$scope.estatisticas = data;
		    }
		});
	};
	$scope.apaguePessoas = function(){
		$http.delete('apaguePessoas').success(function(data) {
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
		    	alert(data.qtdPessoas + " pessoas excluídas com sucesso");
		    	$scope.atualize();
		    }
		});
	};
	$scope.apaguePesquisas = function(){
		$http.delete('apaguePesquisas').success(function(data) {
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
		    	alert("Pesquisas excluídas com sucesso. " + data.qtdPessoas + " pessoas afetadas.");
		    	$scope.atualize();
		    }
		});
	};
	$scope.atualize()
});

encontrosApp.controller('MaisDesejadosCtrl', function ($scope, $http) {
	$scope.numeroDesejadas = 10;
	$scope.atualize = function(numeroDesejadas){
		$http.get('listeMaisDesejadas', { params: { "qtd": numeroDesejadas }}).success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoasDesejadas = data.pessoas;
			}
		});
	};
	$scope.atualize($scope.numeroDesejadas);
});

encontrosApp.controller('PesquisaCtrl', function ($scope, $http) {
	$scope.criterios = {};
	$scope.pesquise = function(criterios){
		$http.post('pesquise', criterios).success(function(data){
			if(data.cod === "NOK"){
				alert(data.erro);
			} else {
				$scope.pessoas = data.pessoas;
			}
		});
	};
});