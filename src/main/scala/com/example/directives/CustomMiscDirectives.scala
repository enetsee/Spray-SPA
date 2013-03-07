package com.example
package directives

import concurrent.{ExecutionContext, Future}
import scala.util.{Success,Failure}

import shapeless.HNil
import shapeless.::

import spray.http.HttpIp
import spray.routing.{Directive, Directive0,Route}
import spray.routing.directives.{RouteDirectives,BasicDirectives,MiscDirectives}
import BasicDirectives._
import RouteDirectives._
import MiscDirectives.{validate,clientIP}



trait CustomMiscDirectives{


  def conditional(check: => Boolean,directive0: Directive0) : Directive0 =
    if (check) directive0
    else new Directive0 { def happly(f: HNil => Route) = f(HNil)}

  def validateFuture(vfm: ValidationFutureMagnet, errorMsg:String ) : Directive0 = {
    implicit def executor = vfm.executor
    vfm.value.unwrapFuture.flatMap { case check => validate(check,errorMsg) }
  }

  def provideFuture[T](pfm: ProvideFutureMagnet[T]) : Directive[T :: HNil] = {
    implicit def executor = pfm.executor
    pfm.value.unwrapFuture.flatMap { provide(_) }
  }


  lazy val optionalClientIP: Directive[Option[HttpIp] :: HNil] =
    clientIP.map(Some(_) : Option[HttpIp]).recoverPF {case Nil => provide(None)}


//  def authenticate[T](am: AuthMagnet[T]): Directive[T :: HNil] = {
//    implicit def executor = am.executor
//    am.value.unwrapFuture.flatMap {
//      case Right(user) => provide(user)
//      case Left(rejection) => reject(rejection)
//    }
//  }

}



object CustomMiscDirectives extends CustomMiscDirectives

class ValidationFutureMagnet(val value:Directive[Future[Boolean]::HNil], val executor:ExecutionContext)

object ValidationFutureMagnet {
  implicit def fromFuture(check: Future[Boolean])(implicit executor:ExecutionContext) =
    new ValidationFutureMagnet(provide(check),executor)
}


class ProvideFutureMagnet[T](val value:Directive[Future[T]::HNil],val executor:ExecutionContext)

object ProvideFutureMagnet {
  implicit def fromFuture[T](value: Future[T])(implicit executor:ExecutionContext) =
    new ProvideFutureMagnet(provide(value),executor)
}