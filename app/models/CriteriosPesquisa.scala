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
	    val alturaMinima = (form \ "altura-minima").as[Int]
	    val alturaMaxima = (form \ "altura-maxima").as[Int]
	    val nome = (form \ "nome").as[String]
	    val sexo = (form \ "sexo").as[String]
	    if(nome == "" && sexo == "" && alturaMaxima == 0 && alturaMinima == 0)
	      Left("Especifique ao menos um filtro")
	    else if(sexo != "" && sexo != "F" && sexo != "M")
	      Left("Sexo inválido")
	    else {
	      if(nome != "")
	        listaCriterios = listaCriterios :+ CriterioNomeContendo(nome)
	      if(sexo != "")
	        listaCriterios = listaCriterios :+ CriterioSexo(sexo.toCharArray()(0))
	      if(alturaMinima != 0 || alturaMaxima != 0)
	        listaCriterios = listaCriterios :+ CriterioAltura(
	            if (alturaMinima == 0) None else Some(alturaMinima), 
	            if (alturaMaxima == 0) None else Some(alturaMaxima))
	      Right(listaCriterios)
	    }
    } catch {
      case e: Exception => {
        Left("Altura inválida")
      }
    }
  }
}