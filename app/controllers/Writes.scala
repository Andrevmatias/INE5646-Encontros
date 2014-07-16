package controllers

import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Pessoa
import models.PessoaDesejada

/**
 * Objeto com os escritores de json necessários para a aplicação
 */
object Writes {
 implicit val pessoaWrites: Writes[Pessoa] = (
      (JsPath \ "cpf").write[Long] and
      (JsPath \ "nome").write[String] and
      (JsPath \ "sexo").write[String] and
      (JsPath \ "altura").write[Int]
  )(unlift(Pessoa.unapply))
  implicit val pessoaDesejadaWrites: Writes[PessoaDesejada] = (
      (JsPath \ "pessoa").write[Pessoa] and
      (JsPath \ "numeroDesejos").write[Int]
  )(unlift(PessoaDesejada.unapply))
}