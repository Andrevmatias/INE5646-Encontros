package models.atores

import akka.actor._
import models.Pessoa
import models.CriterioDePesquisa
import play.libs.Akka
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import scala.util.Random
import configuracao.ParametrosDeExecucao
import utils.CpfUtils

//Objeto de "recursos" para o ator GeradorPessoas
object GeradorPessoas {

  //Método para criar as propriedades de um ator
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(repositorioPessoas: ActorRef, maximo: Int) 
  	= Props(classOf[GeradorPessoas], repositorioPessoas: ActorRef, maximo: Int)
  
  //Trait para classificação de respostas
  trait RespostaGeradorPessoas
  
  //Mensagens disponíveis e
  //Respostas possíveis
  //------------------------------------------
  case class Gerar(qtd: Int)
  case class PessoasRegistradas(qtd: Int) extends RespostaGeradorPessoas
  case class QuantidadeExcedeuMaximo(maximo: Int) extends RespostaGeradorPessoas
  //------------------------------------------
}


class GeradorPessoas(repositorioPessoas: ActorRef, maximo: Int) 
	extends Actor {

  //Import tudo de GeradorPessoas
  import models.atores.GeradorPessoas._
  import context._
  
  private val randomizer = Random
  private val rangeAltura = ParametrosDeExecucao.alturaMaxima - ParametrosDeExecucao.alturaMinima
  private var countCadastradas = 0
  private var qtdCadastrar = 0
  private var firstSender: ActorRef =_
  
  val nomesSexos = Array(("André", 'M'), ("Eliete",'F'), ("Luany", 'F'), 
      ("Flavia", 'F'), ("Fabio", 'M'), ("Udson", 'M'), ("Luiz", 'M'), ("Natália", 'F'))
  val sobrenomes = Array("Matos", "Lourenço", 
      "Meller", "Darós", "Pazzini", "Uribe", "Victória", "Zannete") 
  
  def receive = {
    case Gerar(qtd: Int) => {
      if(qtd > maximo){
        sender ! QuantidadeExcedeuMaximo(maximo)
      }else{
        //TODO Verificar se o cpf não foi duplicado
    		qtdCadastrar = qtd
    		firstSender = sender
    		become(esperandoRespostasPessoasCadastradas)
        repositorioPessoas ! RepositorioPessoas.Save(gerarPessoa)
      }
    }
  }
  
  def esperandoRespostasPessoasCadastradas: Receive = {
  	case RepositorioPessoas.PessoaCadastrada => {
  		countCadastradas += 1
  		if(countCadastradas == qtdCadastrar){
  			firstSender ! PessoasRegistradas(qtdCadastrar)
  			unbecome()
  		} else {
        repositorioPessoas ! RepositorioPessoas.Save(gerarPessoa)
      }
  	}
    case RepositorioPessoas.PessoaJaCadastrada(_) => repositorioPessoas ! RepositorioPessoas.Save(gerarPessoa)
  	case RepositorioPessoas.MaximoPessoasAtingido => {
  		unbecome()
  		firstSender ! PessoasRegistradas(countCadastradas)
  	}
  }
  
  private def gerarPessoa = {
    val nomeSexo = nomesSexos(randomizer.nextInt(nomesSexos.length))
    val sobrenome = sobrenomes(randomizer.nextInt(sobrenomes.length)) + " " + sobrenomes(randomizer.nextInt(sobrenomes.length))
    val nomeCompleto = nomeSexo._1  + " " + sobrenome
    val altura = randomizer.nextInt(rangeAltura) + ParametrosDeExecucao.alturaMinima
    val cpf = CpfUtils.gerarCpf
    Pessoa(cpf, nomeCompleto, nomeSexo._2.toString(), altura)
  }
}