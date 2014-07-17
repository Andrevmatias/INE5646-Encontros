package models.atores

import scala.annotation.migration
import scala.concurrent.duration._
import org.omg.CORBA.Object
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.PessoaDesejada
import play.api.libs.concurrent.Akka
import scala.collection.immutable.ListMap

//Objeto de "recursos" para o ator RegistroDesejos
object RegistroDesejos {
  
  //Método para criar as propriedades de um ator
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
  
  case object Count
  case class QuantidadePessoasDesejadas(qtd: Int)
  
  case object Clear
  case class DesejosRemovidos(qtd: Int)
  
  case object List
  case class DesejosRegistrados(desejos: Map[Long, Int])
  
  case class ListMaisDesejadas(qtd: Int)
  //DesejosRegistrados(desejos: Map[Long, Int])
  //------------------------------------------
}


class RegistroDesejos(repositorioPessoas: ActorRef) extends Actor {
  import RegistroDesejos._
  
  // cpf da pessoa -> quantas vezes uma pessoa com aquele CPF foi desejada
  // se um CPF não está cadastrado então o valor retornado deve ser zero
  var desejosPorCPF = Map[Long, Int]().withDefaultValue(0)

  def receive = {
    case RegistreDesejos(cpfs) => cpfs.foreach(cpf => desejosPorCPF += cpf -> (desejosPorCPF(cpf) + 1))
    case Clear => {
      val numeroDesejos = desejosPorCPF.size
      desejosPorCPF = Map[Long, Int]().withDefaultValue(0)
      sender ! DesejosRemovidos(numeroDesejos)
    }
    case Count => sender ! QuantidadePessoasDesejadas(desejosPorCPF.size)
    case List => sender ! DesejosRegistrados(desejosPorCPF)
    case ListMaisDesejadas(qtd) => sender ! DesejosRegistrados(ListMap(desejosPorCPF.toList sortBy {- _._2} take(qtd):_*))
  }

}