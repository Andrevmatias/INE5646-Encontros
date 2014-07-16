package utils

import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.util.Random

/**
 * Utilitário para criação e validação de CPF
 */
object CpfUtils {
  def valideCPF(optCPF: Option[String]): Either[String, Long] = {
    optCPF match {
      case None => Left("CPF não definido")
      case Some(sCPF) => {
        if (sCPF.trim.length == 0) Left("CPF não definido")
        else
          Try(sCPF.toLong) match {
            case Failure(_) => Left("CPF não é número")
            case (Success(cpf)) => if (cpf < 1) Left("CPF deve ser maior que zero") else Right(cpf)
          }
      }
    }
  }
  def gerarCpf = {
    val randomizer = Random
    val primeiros = (randomizer.nextInt(900000000) + 100000000).toLong
    val digitos = calcularDigitos(primeiros)
    (primeiros * 100) + digitos
  }
  private def calcularDigitos(primeiros: Long) = {
    val primeirosString = primeiros.toString
    var primeiroDigito: Int = 0
    for (i <- 0 to 8)
      primeiroDigito += (i + 1) * primeirosString(i).toInt
    primeiroDigito %= 11
    primeiroDigito %= 10

    var segundoDigito: Int = 0
    for (i <- 1 to 9)
      segundoDigito += i * primeirosString(i % 9).toInt
    segundoDigito %= 11
    segundoDigito %= 10

    primeiroDigito * 10 + segundoDigito
  }
}