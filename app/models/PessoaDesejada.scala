package models

object PessoaDesejada{
  def toJson(pessoaDesejada: PessoaDesejada)={
    s"{pessoa:${Pessoa.toJson(pessoaDesejada.pessoa)},numeroDesejos:${pessoaDesejada.numeroDesejos}}"
  }
}

case class PessoaDesejada(pessoa: Pessoa, numeroDesejos: Int)