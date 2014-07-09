package controllers

import play.api._
import play.api.mvc._
import akka.util.Timeout
import configuracao.ParametrosDeExecucao
import play.libs.Akka
import models.atores.RepositorioPessoas
import akka.actor.Props
import models.atores.RegistroDesejos
import scala.concurrent.duration.`package`.DurationInt

object Application extends Controller {

  import play.api.Play.current
  
  
  val repositorio = Akka.system.actorOf(RepositorioPessoas.props(ParametrosDeExecucao.maximoPessoas))
  val registroDesejos = Akka.system.actorOf(RegistroDesejos.props(repositorio))
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def pesquise = Action.async { implicit request =>
    implicit val timeout = Timeout(10 seconds)

    // para requisições POST o AngularJS envia os dados no formato JSON
    val form = request.body.asJson.get

    ExtratorDeCriterios.extraia(form) match {
      case Left(msgsErro) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> msgsErro)))
      case Right(criterios) => {
        val pesquisador = Akka.system.actorOf(Pesquisador.props(criterios, repositorio, registroDesejos))

        val futResp = (pesquisador ? Pesquisador.Pesquise).mapTo[Pesquisador.PessoasEncontradas]

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

    def valideCPF(optCPF: Option[String]): Either[String, Int] = {
      optCPF match {
        case None => Left("CPF não definido")
        case Some(sCPF) => {
          if (sCPF.trim.length == 0) Left("CPF não definido")
          else
            Try(sCPF.toInt) match {
              case Failure(_) => Left("CPF não é número")
              case (Success(cpf)) => if (cpf < 1) Left("CPF deve ser maior que zero") else Right(cpf)
            }
        }
      }
    }

    valideCPF(request.getQueryString("cpf")) match {
      case Left(msgErro) => Future.successful(Ok(Json.obj("cod" -> "NOK", "erro" -> msgErro)))
      case Right(cpf) => {
        val futResp = (cadastro ? Cadastro.LeiaPessoa(cpf)).mapTo[Cadastro.RespostaCadastro]

        val resultado = futResp.map(resp => resp match {
          case Cadastro.PessoaNaoCadastrada(cpf) => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não existe pessoa com este CPF"))
          case Cadastro.PessoaLida(pessoa) => Ok(Json.obj("cod" -> "OK", "pessoa" -> Pessoa.toJson(pessoa)))
        }) recover {
          case _ => Ok(Json.obj("cod" -> "NOK", "erro" -> "Não conseguiu ler"))
        }

        resultado

      }
    }
  }

}