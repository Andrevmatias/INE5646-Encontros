package controllers

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import models.Pessoa

/**
 * Objeto com os leitores de json necessários para a aplicação
 */
object Reads {
 implicit val pessoaReads: Reads[Pessoa] = (
      (__ \ "cpf").read[Long] and
      (__ \ "nome").read[String](minLength[String](1)) and
      (__ \ "sexo").read[String] and
      (__ \ "altura").read[Int]
  )(Pessoa.apply _)
}