package com.example
package directives

import concurrent.{ExecutionContext, Future}

import shapeless.HNil
import shapeless.::

import spray.routing.{Directive, Directive0,Route}
import spray.routing.directives.{BasicDirectives,MiscDirectives}
import BasicDirectives._
import MiscDirectives.{validate,clientIP}
import spray.http.HttpIp


trait CustomMiscDirectives{


  def conditional(check: => Boolean,directive0: Directive0) : Directive0 =
    if (check) directive0
    else new Directive0 { def happly(f: HNil => Route) = f(HNil)}

  def validateFuture(vfm: ValidationFutureMagnet, errorMsg:String ) : Directive0 = {
    implicit def executor = vfm.executor
    vfm.value.unwrapFuture.flatMap { case check => validate(check,errorMsg) }
  }

  lazy val optionalClientIP: Directive[Option[HttpIp] :: HNil] =
    clientIP.map(Some(_) : Option[HttpIp]).recoverPF {case Nil => provide(None)}


}



object CustomMiscDirectives extends CustomMiscDirectives

class ValidationFutureMagnet(val value:Directive[Future[Boolean]::HNil], val executor:ExecutionContext)

object ValidationFutureMagnet {
  implicit def fromFuture(check: Future[Boolean])(implicit executor:ExecutionContext) =
    new ValidationFutureMagnet(provide(check),executor)
}


