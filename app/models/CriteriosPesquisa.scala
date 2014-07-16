package models

import configuracao.ParametrosDeExecucao
import play.api.libs.json.JsValue

//Cria um trait para critério
trait CriterioDePesquisa{
  //Todo critério deve saber "se aplicar"
  def aplicar(pessoas: List[Pessoa]):List[Pessoa]
}

//Se existir apenas mínima, maior que, se existir apenas máxima, menor que, se ambas, entre
case class CriterioAltura(min: Option[Int], max: Option[Int]) extends CriterioDePesquisa{
  def aplicar(pessoas: List[Pessoa]):List[Pessoa] = {
    //Cria uma tupla (,) para comparação de padrões
    (min, max) match {
      //Caso os dois estejam definidos
      case (Some(min), Some(max)) => pessoas filter(pessoa => pessoa.altura >= min && pessoa.altura <= max)
      //Caso apenas o max esteja definido
      case (None, Some(max)) => pessoas filter(pessoa => pessoa.altura <= max)
      //Caso apenas o min esteja definido
      case (Some(min), None) => pessoas filter(pessoa => pessoa.altura >= min)
      //Ambos none não realiza filtro (a menos que seja uma função parcial, uma função deve definir seu valor para todo o domínio)
      case (None, None) => pessoas
    }
  }
}
case class CriterioSexo(s: Char) extends CriterioDePesquisa{
  def aplicar(pessoas: List[Pessoa]):List[Pessoa] = {
    if(s == 'M' || s == 'F')
      pessoas filter(pessoa => pessoa.sexo == s)
    else
      List()
  }
}
case class CriterioNomeContendo(fragNome: String) extends CriterioDePesquisa{
  def aplicar(pessoas: List[Pessoa]):List[Pessoa] = {
      pessoas filter(pessoa => pessoa.nome.contains(fragNome))
  }
}
object ExtratorDeCriterios {
  def extraia(form: JsValue): Either[String, List[CriterioDePesquisa]] = {
    var listaCriterios = List[CriterioDePesquisa]()
    try{
	    val alturaMinima = (form \ "alturaMinima").asOpt[Int]
	    val alturaMaxima = (form \ "alturaMaxima").asOpt[Int]
	    val nome = (form \ "nome").asOpt[String]
	    val sexo = (form \ "sexo").asOpt[String]
	    if(nome.isEmpty && sexo.isEmpty && alturaMaxima.isEmpty && alturaMinima.isEmpty)
	      Left("Especifique ao menos um filtro")
	    else if(!sexo.isEmpty && sexo.get != "F" && sexo.get != "M")
	      Left("Sexo inválido")
	    else {
	      if(!nome.isEmpty)
	        listaCriterios = listaCriterios :+ CriterioNomeContendo(nome.get)
	      if(!sexo.isEmpty)
	        listaCriterios = listaCriterios :+ CriterioSexo(sexo.get.toCharArray()(0))
	      if(!alturaMinima.isEmpty || !alturaMaxima.isEmpty)
	        listaCriterios = listaCriterios :+ CriterioAltura(alturaMinima, alturaMaxima)
	      Right(listaCriterios)
	    }
    } catch {
      case e: Exception => {
        Left("Altura inválida")
      }
    }
  }
}