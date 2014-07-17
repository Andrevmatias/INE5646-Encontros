package configuracao

//Singleton com os parâmetros de execução do sistema conforme requisitos
case object ParametrosDeExecucao {
	val maximoPessoas = 3000000
	val alturaMaxima = 215
	val alturaMinima= 140
	val maximoPessoasGeradas = 400000
	val maximoPesquisaPesoasDesejadas = 100
	val nomeDesenvolvedor = "André Victória Matias"
	  
	def toStringValueMap = {
	  Map(
	     "maximoPessoas" -> maximoPessoas.toString,
	     "alturaMaxima" -> alturaMaxima.toString,
	     "alturaMinima" -> alturaMinima.toString,
	     "maximoPessoasGeradas" -> maximoPessoasGeradas.toString, 
	     "maximoPesquisaPesoasDesejadas" -> maximoPesquisaPesoasDesejadas.toString,
	     "nomeDesenvolvedor" -> nomeDesenvolvedor
	  )
	}
}