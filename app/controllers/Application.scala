package controllers

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import akka.actor.PoisonPill
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import configuracao.ParametrosDeExecucao
import models.ExtratorDeCriterios
import models.Pessoa
import models.atores.Pesquisador
import models.atores.RegistroDesejos
import models.atores.RepositorioPessoas
import play.api._
import play.api.mvc._
import play.libs.Akka
import utils.CpfUtils
import controllers.Reads._
import controllers.Writes._
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import models.atores.PesquisadorPessoasDesejadas
import models.PessoaDesejada
import models.atores.GeradorPessoas

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  import play.api.Play.current
  
  
  val repositorio = Akka.system.actorOf(RepositorioPessoas.props(ParametrosDeExecucao.maximoPessoas))
  val registroDesejos = Akka.system.actorOf(RegistroDesejos.props(repositorio))
  
  def index = Action {
    Ok(views.html.index("Encontros"))
  }
  
  def pesquise = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)

    // para requisições POST o AngularJS envia os dados no formato JSON
    val form = request.body.asJson.get

    ExtratorDeCriterios.extraia(form) match {
      case Left(msgsErro) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> msgsErro)))
      case Right(criterios) => {
        val pesquisador = Akka.system.actorOf(Pesquisador.props(repositorio, registroDesejos))

        val futResp = (pesquisador ? Pesquisador.Pesquisar(criterios)).mapTo[Pesquisador.PessoasEncontradas]

        val resultado = futResp.map(msg => {
          val r = for (pessoa <- msg.pessoas) yield Pessoa.toJson(pessoa)
          Ok(Json.obj("cod" -> "OK", "pessoas" -> Json.toJson(r)))
        }).recover {
          case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu pesquisar"))
        }

        futResp.onComplete { case _ => pesquisador ! PoisonPill }
        resultado
      }
    }
  }
    
  def leiaPessoa = Action.async { implicit request =>
    implicit val timeout = Timeout(1 second)

    CpfUtils.valideCPF(request.getQueryString("cpf")) match {
      case Left(msgErro) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> msgErro)))
      case Right(cpf) => {
        val futResp = (repositorio ? RepositorioPessoas.Get(cpf)).mapTo[RepositorioPessoas.RespostaRepositorio]

        val resultado = futResp.map(resp => resp match {
          case RepositorioPessoas.PessoaNaoCadastrada(cpf) => Ok(Json.obj("cod" -> "NOK", "erro" -> ("Não existe pessoa com este CPF: " + cpf.toString())))
          case RepositorioPessoas.PessoaLida(pessoa) => Ok(Json.obj("cod" -> "OK", "pessoa" -> Json.toJson(pessoa)))
        }) recover {
          case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu ler"))
        }

        resultado

      }
    }
  }
  
  def cadastre = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)

    val form = request.body.asJson.get
    form.validate[Pessoa] match {
      case JsError(erros) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> "Existem valores inválidos")))
      case JsSuccess(pessoa, _) => {
        val futResp = (repositorio ? RepositorioPessoas.Save(pessoa)).mapTo[RepositorioPessoas.RespostaRepositorio]
        futResp.map(msg => msg match {
          case RepositorioPessoas.PessoaCadastrada => Ok(Json.obj("cod" -> "OK"))
		  case RepositorioPessoas.MaximoPessoasAtingido => 
		    Ok(Json.obj("cod" -> "NOK", "erro" -> "O máximo de pessoas cadastradas foi atingido"))
		  case RepositorioPessoas.AlturaForaDosLimites(minima, maxima) => 
		    Ok(Json.obj("cod" -> "NOK", "erro" -> s"A altura está fora dos limtes: $minima - $maxima"))
		  case RepositorioPessoas.PessoaJaCadastrada(pessoa) => 
		    Ok(Json.obj("cod" -> "NOK", "erro" -> "Pessoa já cadastrada", "pessoa" -> Pessoa.toJson(pessoa)))
		  case RepositorioPessoas.PessoaComMesmoNomeDesenvolvedor(nomeDesenvolvedor) => 
		    Ok(Json.obj("cod" -> "NOK", "erro" -> s"O desenvolvedor não pode ser cadastrado: $nomeDesenvolvedor"))
        }).recover {
          case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu cadastrar"))
        }
      }
    }
  }
  
  def listeMaisDesejadas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)
    request.getQueryString("qtd") match {
      case None => Future { Ok(Json.obj("cod" -> "NOK", "erro" -> "Digite uma quantidade válida")) }
      case Some(qtd) => {
        val pesquisador = Akka.system.actorOf(PesquisadorPessoasDesejadas.props(repositorio, registroDesejos))
        val futResp = (pesquisador ? PesquisadorPessoasDesejadas.PessoasMaisDesejadas(qtd.toInt))
        	.mapTo[PesquisadorPessoasDesejadas.RespostaPesquisadorPessoasDesejadas]
        futResp.map(msg => msg match {
          case PesquisadorPessoasDesejadas.PessoasDesejadas(pessoasDesejadas) => {
	           val r = for (pessoa <- pessoasDesejadas) yield Json.toJson(pessoa)
	           Ok(Json.obj("cod" -> "OK", "pessoas" -> Json.toJson(r)))
          }
        })
      }
    }
  }
  
  def listeTodas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 second)
    val futResp = (repositorio ? RepositorioPessoas.List).mapTo[RepositorioPessoas.RespostaRepositorio]
    futResp.map(msg => msg match {
      case RepositorioPessoas.PessoasLidas(pessoas) => {
          val r = for (pessoa <- pessoas) yield Json.toJson(pessoa)
          Ok(Json.obj("cod" -> "OK", "pessoas" -> Json.toJson(r)))
      }
    })
  }
  
  def desejadasAoMenosUmaVez = Action.async { implicit request =>
    implicit val timeout = Timeout(10 second)

    val pesquisador = Akka.system.actorOf(PesquisadorPessoasDesejadas.props(repositorio, registroDesejos))
    val futResp = (pesquisador ? PesquisadorPessoasDesejadas.PessoasDesejadasAoMenosUmaVez)
    	.mapTo[PesquisadorPessoasDesejadas.RespostaPesquisadorPessoasDesejadas]
    futResp.map(msg => msg match {
      case PesquisadorPessoasDesejadas.PessoasDesejadas(pessoasDesejadas) => {
           val r = for (pessoa <- pessoasDesejadas) yield Json.toJson(pessoa)
           Ok(Json.obj("cod" -> "OK", "pessoas" -> Json.toJson(r)))
      }
    }).recover {
      case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu recuperar desejados"))
    }
  }
  
  def gerePessoas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)

    val form = request.body.asJson.get
    form.validate[Int] match {
      case JsError(erros) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> "Quantidade inválida")))
      case JsSuccess(qtd, _) => {
        val gerador = Akka.system.actorOf(GeradorPessoas.props(repositorio, ParametrosDeExecucao.maximoPessoasGeradas))
        val futResp = (gerador ? GeradorPessoas.Gerar(qtd)).mapTo[GeradorPessoas.RespostaGeradorPessoas]
	    futResp.map(msg => msg match {
	      case GeradorPessoas.QuantidadeExcedeuMaximo(maximo) => 
	        Ok(Json.obj("cod" -> "NOK", "erro" -> s"A quantidade não pode passar de $maximo"))
	      case GeradorPessoas.PessoasRegistradas(qtdGeradas) => {
	           Ok(Json.obj("cod" -> "OK", "qtdGeradas" -> qtdGeradas))
	      }
	    }).recover {
	      case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu gerar pessoas"))
	    }
      }
    }
  }
  
  def mostreEstatisticas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)

    (for{
    	respRep <- (repositorio ? RepositorioPessoas.Count).mapTo[RepositorioPessoas.QuantidadePessoas]
    	respDesej <- (registroDesejos ? RegistroDesejos.Count).mapTo[RegistroDesejos.QuantidadePessoasDesejadas]
    } yield {
      Ok(Json.obj("cod" -> "OK", "qtdPessoas" -> respRep.qtd, "qtdPesquisadas" -> respDesej.qtd))
    }).recover {
      case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu recuperar estatísticas"))
    }
  }
  
  def mostreInformacoesSistema = Action { implicit request =>
    Ok(Json.toJson(ParametrosDeExecucao.toJson))
  }
  
  def apaguePessoas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 second)

    (for{
    	respDesej <- (registroDesejos ? RegistroDesejos.Clear)
    	respRep <- (repositorio ? RepositorioPessoas.Clear).mapTo[RepositorioPessoas.PessoasRemovidas]
    } yield {
      Ok(Json.obj("cod" -> "OK", "qtdPessoas" -> respRep.qtd))
    }).recover {
      case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu apagar"))
    }
  }
  
  def apaguePesquisas = Action.async { implicit request =>
    implicit val timeout = Timeout(10 second)

    (registroDesejos ? RegistroDesejos.Clear).mapTo[RegistroDesejos.DesejosRemovidos]
		.map(msg => Ok(Json.obj("cod" -> "OK", "qtdPessoas" -> msg.qtd)))
		.recover {
	      case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu apagar"))
	    }
  }
}