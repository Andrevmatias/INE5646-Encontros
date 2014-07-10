package models.atores

import scala.annotation.migration
import scala.concurrent.duration._

import org.omg.CORBA.Object

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.PessoaDesejada
import play.api.libs.concurrent.Akka

//Objeto de "recursos" para o ator RegistroDesejos
object RegistroDesejos {
  
  //Método para criar as propriedades de um repositório
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(repositorioPessoas: ActorRef) = Props(classOf[RegistroDesejos], repositorioPessoas: ActorRef)

  //Trait para classificação de respostas
  trait RespostaRegistroDesejos
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class RegistreDesejos(cpfs: List[Long])
  
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
  import play.api.Play.current
  
  // cpf da pessoa -> quantas vezes uma pessoa com aquele CPF foi desejada
  // se um CPF não está cadastrado então o valor retornado deve ser zero
  var desejosPorCPF = Map[Long, Int]().withDefaultValue(0)

  def receive = {
    case RegistreDesejos(cpfs) => cpfs.foreach(cpf => desejosPorCPF += cpf -> (desejosPorCPF(cpf) + 1))
    case Clear => { desejosPorCPF = Map() }
    case Count => sender ! QuantidadeDePessoasDesejadas(desejosPorCPF.size)
    case PessoasDesejadasAoMenosUmaVez => { 
      implicit val context = Akka.system.dispatcher
      implicit val timeout = Timeout(10 seconds)
      (repositorioPessoas ? RepositorioPessoas.List).mapTo[RepositorioPessoas.RespostaRepositorio].map(e => e match {
        case RepositorioPessoas.PessoasCadastradas(pessoas) => {
          val pessoasDesejadas = pessoas.map(pessoa => PessoaDesejada(pessoa, desejosPorCPF(pessoa.cpf)))
          sender ! PessoasDesejadas(pessoasDesejadas)
        }
      })
    }
  }

}