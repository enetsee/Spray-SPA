package com.example
package domain
package ajax

import EmailModule._

case class AjaxSignIn(email:Email,password:Password,rememberMe: Boolean)
object AjaxSignIn extends AjaxSignInJsonProtocol

trait AjaxSignInJsonProtocol extends PasswordJsonProtocol {
  implicit val ajaxSignInFormat = jsonFormat3(AjaxSignIn.apply)
}


