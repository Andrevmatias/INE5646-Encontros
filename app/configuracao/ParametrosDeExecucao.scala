package configuracao

//Singleton com os parâmetros de execução do sistema conforme requisitos
object ParametrosDeExecucao {
	val maximoPessoas = 3000000
	val alturaMaxima = 215
	val alturaMinima= 140
	val maximoPessoasGeradas = 400000
	val maximoPesquisaPesoasDesejadas = 100
	val nomeDesenvolvedor = "André Victória Matias"
	  
	def toJson = {
	  s"{maximoPessoas:$maximoPessoas, alturaMaxima:$alturaMaxima, alturaMinima:$alturaMinima," + 
	  	s"maximoPessoasGeradas:$maximoPessoasGeradas, maximoPesquisaPesoasDesejadas:$maximoPesquisaPesoasDesejadas," +
	  	s"nomeDesenvolvedor:$nomeDesenvolvedor}"
	}
}