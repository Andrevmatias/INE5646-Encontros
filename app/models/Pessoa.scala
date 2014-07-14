package models

object Pessoa{
  def toJson(pessoa:Pessoa)={
    Map(
        "cpf" -> pessoa.cpf.toString,
        "nome" -> pessoa.nome,
        "sexo" -> pessoa.sexo,
        "altura" -> pessoa.altura.toString
    )
  }
}

case class Pessoa(cpf: Long, nome: String, sexo: String, altura: Int)