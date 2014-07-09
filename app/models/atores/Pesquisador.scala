package models.atores

import akka.actor.Actor
import akka.actor.Props
import models.Pessoa

//Objeto de "recursos" para o ator Pesquisador
object Pesquisador {

  //Trait para classificação de respostas
  trait RespostaPesquisador
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class Pesquisar(crits: List[CriterioDePesquisa])
  case class PessoasCadastradas(pessoas: List[Pessoa]) extends RespostaPesquisador
  //------------------------------------------
}


class Pesquisador extends Actor {

  //Import tudo de RepositorioPessoas
  import models.atores.Pesquisador._
  
  def receive = {
    case Pesquisar(crits) => {
      sender ! PessoasCadastradas(pessoas.values.toList)
    }
  } 
}