package com.example
package domain
package ajax

case class AjaxUpdatePassword(password:Password, newPassword:Password)

object AjaxUpdatePassword extends AjaxUpdatePasswordJsonProtocol

trait AjaxUpdatePasswordJsonProtocol extends PasswordJsonProtocol {
    implicit val ajaxUpdateFormat = jsonFormat2(AjaxUpdatePassword.apply)
}
