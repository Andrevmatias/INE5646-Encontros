package models.atores

import scala.annotation.migration
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import configuracao.ParametrosDeExecucao
import models.Pessoa
import play.libs.Akka
import akka.actor.ActorRef
import models.PessoaDesejada
import org.omg.CORBA.Object
import scala.concurrent.Future

//Objeto de "recursos" para o ator RegistroDesejos
object RegistroDesejos {
  
  //Método para criar as propriedades de um repositório
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(repositorioPessoas: ActorRef) = Props(classOf[RepositorioPessoas], repositorioPessoas: ActorRef)

  //Trait para classificação de respostas
  trait RespostaRegistroDesejos
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class RegistreDesejos(cpfs: List[Int])
  
  case object PessoasDesejadasAoMenosUmaVez
  case class PessoasDesejadas(pessoas: List[PessoaDesejada])
  
  case object Count
  case class QuantidadeDePessoasDesejadas(qtd: Int)
  
  case object Clear
  case class DesejosRemovidos(qtd: Int)
  //------------------------------------------
}


class RegistroDesejos(repositorioPessoas: ActorRef) extends Actor {
  import RegistroDesejos._

  // cpf da pessoa -> quantas vezes uma pessoa com aquele CPF foi desejada
  // se um CPF não está cadastrado então o valor retornado deve ser zero
  var desejosPorCPF = Map[Int, Int]().withDefaultValue(0)

  def receive = {
    case RegistreDesejos(cpfs) => cpfs.foreach(cpf => desejosPorCPF += cpf -> (desejosPorCPF(cpf) + 1))
    case Clear => { desejosPorCPF = Map() }
    case Count => sender ! QuantidadeDePessoasDesejadas(desejosPorCPF.size)
    case PessoasDesejadasAoMenosUmaVez => { 
        Future.traverse(desejosPorCPF)((cpf, desejos) => 
        	  (repositorioPessoas ? RepositorioPessoas.Get(cpf)).map(e => e match {
        	  	  case RepositorioPessoas.PessoaLida(pessoa) => PessoaDesejada(pessoa, desejos)
    	  	  }))
        	.map(resp => { 
        	  sender ! PessoasDesejadas(resp.toList())
        	})
    }
  }

}