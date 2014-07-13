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

//Objeto de "recursos" para o ator GeradorPessoas
object GeradorPessoas {

  //Método para criar as propriedades de um ator
  /* No modelo de atores do akka, isto precisa ser feito caso existam parâmetros para inicializar um ator.
   * Como não é possível ter acesso ao construtor do ator, passa-se um objeto Props para o Factory de atores
   * (system.actorOf()) com os parâmetros de inicialização
   */
  def props(criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, maximo: Int) 
  	= Props(classOf[Pesquisador], criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, maximo: Int)
  
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


class GeradorPessoas(criterios: List[CriterioDePesquisa], repositorioPessoas: ActorRef, maximo: Int) 
	extends Actor {

  //Import tudo de GeradorPessoas
  import models.atores.GeradorPessoas._
  
  private val randomizer = Random
  private val rangeAltura = ParametrosDeExecucao.alturaMaxima - ParametrosDeExecucao.alturaMinima
  
  val nomesSexos = Array(("André", 'M'), ("Eliete",'F'), ("Luany", 'F'), 
      ("Flavia", 'F'), ("Fabio", 'M'), ("Udson", 'M'), ("Luiz", 'M'), ("Natália", 'F'))
  val sobrenomes = Array("Matos", "Lourenço", 
      "Meller", "Darós", "Pazzini", "Uribe", "Victória", "Matias") 
  
  def receive = {
    case Gerar(qtd: Int) => {
      if(qtd > maximo){
        sender ! QuantidadeExcedeuMaximo(maximo)
      }else{
        //TODO Verificar se o cpf não foi duplicado
        for(i <- 1 to qtd)
          repositorioPessoas ! RepositorioPessoas.Save(gerarPessoa)
        sender ! PessoasRegistradas(qtd)
      }
    }
  }
  
  private def gerarPessoa = {
    val nomeSexo = nomesSexos(randomizer.nextInt(nomesSexos.length))
    val sobrenome = sobrenomes(randomizer.nextInt(sobrenomes.length)) + " " + sobrenomes(randomizer.nextInt(sobrenomes.length))
    val nomeCompleto = nomeSexo._1  + " " + sobrenome
    val altura = randomizer.nextFloat() * rangeAltura + ParametrosDeExecucao.alturaMinima
    val cpf = gerarCpf
    Pessoa(cpf, nomeCompleto, nomeSexo._2, altura)
  }
  
  private def gerarCpf = {
    val primeiros = (randomizer.nextInt(900000000) + 100000000).toLong
    val digitos = gerarDigitos(primeiros)
    (primeiros * 100) + digitos
  }
  private def gerarDigitos(primeiros: Long) ={
    val primeirosString = primeiros.toString
    var primeiroDigito: Int = 0
	for(i <- 0 to 8)
	  primeiroDigito += (i + 1) * primeirosString(i).toInt
	primeiroDigito %= 11
	primeiroDigito %= 10
	
	var segundoDigito: Int = 0
	for(i <- 1 to 9)
	  segundoDigito += i * primeirosString(i % 9).toInt
	segundoDigito %= 11
	segundoDigito %= 10
    
	primeiroDigito * 10 + segundoDigito
  }
}