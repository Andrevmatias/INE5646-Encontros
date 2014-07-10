package models

object Pessoa{
  def toJson(pessoa:Pessoa)={
    s"{cpf:${pessoa.cpf},nome:${pessoa.nome},sexo:${pessoa.sexo},altura:${pessoa.altura}"
  }
}

case class Pessoa(cpf: Long, nome: String, sexo: Char, altura: Float)