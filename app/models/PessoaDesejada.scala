package models

object PessoaDesejada{
  def toJson(pessoaDesejada: PessoaDesejada)={
    Map(
        "pessoa" -> Pessoa.toJson(pessoaDesejada.pessoa).toString,
        "numeroDesejos" -> pessoaDesejada.numeroDesejos.toString
    )
  }
}

case class PessoaDesejada(pessoa: Pessoa, numeroDesejos: Int)