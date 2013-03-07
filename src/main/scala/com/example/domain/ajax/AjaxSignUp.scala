package com.example
package domain
package ajax


import NameModule._
import EmailModule._

case class AjaxSignUp(name:Name,email:Email,password:Password)

object AjaxSignUp extends AjaxSignUpJsonProtocol with AjaxSignUpImplicitsLow

trait AjaxSignUpJsonProtocol extends PasswordJsonProtocol {
  implicit val ajaxSignUpFormat = jsonFormat3(AjaxSignUp.apply)
}

trait AjaxSignUpImplicitsLow {
  implicit def signUpToAccount(signUp:AjaxSignUp): Account =  Account(name=signUp.name,email=signUp.email,password=signUp.password)
}
