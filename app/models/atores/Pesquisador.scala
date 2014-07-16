package models.atores

import akka.actor._
import models.Pessoa
import models.CriterioDePesquisa
import play.libs.Akka
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask

//Objeto de "recursos" para o ator Pesquisador
object Pesquisador {

  //Método para criar as propriedades de um repositório
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(repositorioPessoas: ActorRef, registroDesejos: ActorRef) 
  	= Props(classOf[Pesquisador], repositorioPessoas: ActorRef, registroDesejos: ActorRef)
  
  //Trait para classificação de respostas
  trait RespostaPesquisador
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class Pesquisar(crits: List[CriterioDePesquisa])
  case class PessoasEncontradas(pessoas: List[Pessoa]) extends RespostaPesquisador
  //------------------------------------------
}


class Pesquisador(repositorioPessoas: ActorRef, registroDesejos: ActorRef) 
	extends Actor {

  //Import tudo de Pesquisador
  import models.atores.Pesquisador._
  import context._
  
  def esperandoPessoas(emissor: ActorRef, criterios: List[CriterioDePesquisa]) : Receive = {
    case RepositorioPessoas.PessoasLidas(pessoas) => {
      val pessoasCadastradas = criterios.foldLeft(pessoas.toList)((pessoas, criterio) => criterio.aplicar(pessoas))
      registroDesejos ! RegistroDesejos.RegistreDesejos(pessoasCadastradas.map(pessoa => pessoa.cpf))
      emissor ! PessoasEncontradas(pessoasCadastradas)
      unbecome()
    }
  }
  
  def receive = {
    case Pesquisar(crits) => {
      become(esperandoPessoas(sender, crits))
      repositorioPessoas ! RepositorioPessoas.List
    }
  } 
}