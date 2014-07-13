package models.atores

import scala.annotation.migration

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import configuracao.ParametrosDeExecucao
import models.Pessoa
import play.libs.Akka

//Objeto de "recursos" para o ator RepositorioPessoas
object RepositorioPessoas {
  
   //Método para criar as propriedades de um repositório
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(qtdMaximaPessoas: Int) = Props(classOf[RepositorioPessoas], qtdMaximaPessoas)

  //Trait para classificação de respostas
  trait RespostaRepositorio
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class Save(pessoa: Pessoa)
  case object PessoaCadastrada
  case object MaximoPessoasAtingido
  case class PessoaJaCadastrada(pessoa: Pessoa)
  
  case class Get(cpf: Long)
  case class PessoaLida(pessoa: Pessoa) extends RespostaRepositorio
  case class PessoaNaoCadastrada(cpf: Long) extends RespostaRepositorio
  
  case class GetMany(cpfs: Iterable[Long])
  case class PessoasLidas(pessoa: Iterable[Pessoa]) extends RespostaRepositorio
  
  case object Clear
  case class PessoasRemovidas(qtd: Int) extends RespostaRepositorio
  
  case object List
  case class PessoasCadastradas(pessoas: List[Pessoa]) extends RespostaRepositorio
  //------------------------------------------
}


class RepositorioPessoas(qtdMaximaPessoas: Int) extends Actor {

  //Import tudo de RepositorioPessoas
  import models.atores.RepositorioPessoas._
  
  var pessoas = Map[Long, Pessoa]()
  
  def receive = {
    case Save(pessoa) => {
      if (pessoas.size == qtdMaximaPessoas){
        sender ! MaximoPessoasAtingido
      } else if (pessoas.contains(pessoa.cpf)){
        sender ! PessoaJaCadastrada(pessoas(pessoa.cpf))
      }else{
        pessoas += (pessoa.cpf -> pessoa)
        sender ! PessoaCadastrada
      }
    }
    case Clear => {
      //Envia ("!" é um método) um objeto PessoasRemovidas com o número de pessoas removidas para quem chamou
      sender ! PessoasRemovidas(pessoas.size)
      //Sobreescreve o mapeamento de pessoas com um Map vazio
      pessoas = Map()
    }

    case Get(cpf) => {
      if (pessoas.contains(cpf))
        sender ! PessoaLida(pessoas(cpf))
      else
        sender ! PessoaNaoCadastrada(cpf)
    }
    
    case GetMany(cpfs) => {
      val pessoasCadastradas = for(cpf <- cpfs) yield pessoas(cpf)
      sender ! PessoasLidas(pessoasCadastradas)
    }

    case List => sender ! PessoasCadastradas(pessoas.values.toList)
  } 
}