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
      (JsPath \ "cpf").read[Long] and
      (JsPath \ "nome").read[String] and
      (JsPath \ "sexo").read[String] and
      (JsPath \ "altura").read[Int]
  )(Pessoa.apply _)
}