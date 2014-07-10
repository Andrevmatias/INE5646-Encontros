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
  def props(criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, registroDesejos: ActorRef) 
  	= Props(classOf[Pesquisador], criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, registroDesejos: ActorRef)
  
  //Trait para classificação de respostas
  trait RespostaPesquisador
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class Pesquisar(crits: List[CriterioDePesquisa])
  case class PessoasEncontradas(pessoas: List[Pessoa]) extends RespostaPesquisador
  //------------------------------------------
}


class Pesquisador(criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, registroDesejos: ActorRef) 
	extends Actor {

  //Import tudo de RepositorioPessoas
  import models.atores.Pesquisador._
  
  def receive = {
    case Pesquisar(crits) => {
      implicit val context = Akka.system.dispatcher
      implicit val timeout = Timeout(10 seconds)
      (repositorioPessoas ? RepositorioPessoas.List).mapTo[RepositorioPessoas.RespostaRepositorio].map(e => e match {
        case RepositorioPessoas.PessoasCadastradas(pessoas) => {
          val pessoasCadastradas = criterios.foldLeft(pessoas)((pessoas, criterio) => criterio.aplicar(pessoas))
          registroDesejos ! RegistroDesejos.RegistreDesejos(pessoasCadastradas.map(pessoa => pessoa.cpf))
          sender ! PessoasEncontradas(pessoasCadastradas)
        }
      })
    }
  } 
}