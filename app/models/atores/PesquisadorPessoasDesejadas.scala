package models.atores

import scala.annotation.migration
import scala.concurrent.duration._

import org.omg.CORBA.Object

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.PessoaDesejada
import play.api.libs.concurrent.Akka

//Objeto de "recursos" para o ator PesquisadorPessoasDesejadas
object PesquisadorPessoasDesejadas {
  
  //Método para criar as propriedades de um ator
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(repositorioPessoas: ActorRef, registroDesejos: ActorRef) 
  	= Props(classOf[RegistroDesejos], repositorioPessoas: ActorRef, registroDesejos: ActorRef)

  //Trait para classificação de respostas
  trait RespostaPesquisadorPessoasDesejadas
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case object PessoasDesejadasAoMenosUmaVez
  case class PessoasDesejadas(pessoas: Iterable[PessoaDesejada]) extends RespostaPesquisadorPessoasDesejadas
  
  case class PessoasMaisDesejadas(qtd: Int)
  //PessoasDesejadas(pessoas: Iterable[PessoaDesejada])
  //------------------------------------------
}


class PesquisadorPessoasDesejadas(repositorioPessoas: ActorRef, registroDesejos: ActorRef) extends Actor {
  import PesquisadorPessoasDesejadas._
  import RepositorioPessoas._
  import RegistroDesejos._
  import context._
  
  var firstSender: ActorRef = _
  var desejos: Map[Long, Int] = _

  /*Sequência de estados:
   * inicial -> esperandoDesejos -> esperandoPessoas -> inicial
   */
  
  //Estado inicial
  def receive = {
    case PessoasDesejadasAoMenosUmaVez => { 
      firstSender = sender
      //Muda o estado do ator
      become(esperandoDesejos)
      registroDesejos ! RegistroDesejos.List
    }
    case PessoasMaisDesejadas(qtd) => { 
      firstSender = sender
      //Muda o estado do ator
      become(esperandoDesejos)
      registroDesejos ! RegistroDesejos.ListMaisDesejadas(qtd)
    }
  }
  
  //Receptor de mensagens quando o ator está no estado "esperandoPessoas"
  def esperandoPessoas : Receive = {
    case PessoasLidas(pessoas) => {
    	val pessoasDesejadas = for(pessoa <- pessoas) yield PessoaDesejada(pessoa, desejos(pessoa.cpf))
    	firstSender ! PessoasDesejadas(pessoasDesejadas)
    	//Volta o ator para o estado anterior
    	unbecome()
    }
  }
  
  //Receptor de mensagens quando o ator está no estado "esperandoDesejos"
  def esperandoDesejos : Receive = {
    case DesejosRegistrados(desejosRegistrados) => {
    	desejos = desejosRegistrados
    	//Volta o ator para o estado anterior
    	unbecome()
    	//Muda o ator para o estado "esperandoPessoas"
    	become(esperandoPessoas)
    	repositorioPessoas ! RepositorioPessoas.GetMany(desejos.keys)
    }
  }

}