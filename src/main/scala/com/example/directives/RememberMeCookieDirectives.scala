package com.example
package directives

import shapeless.HNil
import shapeless.::

import spray.routing.directives._
import spray.http.HttpCookie
import spray.http.HttpHeaders.Cookie
import spray.routing.{Directive,Directive0}

import util.AsString
import cookies.RememberMeCookie
import rejections.CustomRejections.MissingRememberMeCookieRejection



trait RememberMeCookieDirectives {
  import RouteDirectives._
  import HeaderDirectives._
  import CookieDirectives._

  type RememberId
  implicit def ev: AsString[RememberId]

  def rememberMeCookie: Directive[RememberMeCookie[RememberId] :: HNil] = headerValue {
    case Cookie(cookies) => cookies.find(_.name == SiteSettings.RememberCookieName) flatMap { rememberMeFromCookie }
    case _ => None
  } | reject(MissingRememberMeCookieRejection)

  def deleteRememberMe(domain: String = "", path: String = ""): Directive0 =
    deleteCookie(SiteSettings.RememberCookieName, domain, path)

  def setRememberMe(remember: RememberMeCookie[RememberId]): Directive0 =
    setCookie(rememberMeToCookie(remember))


  private def encode(id:RememberId,seriesToken:String,rememberToken:String) =
    s"${ev.toString(id)}:$seriesToken:$rememberToken"


  private def decode(content:String): Option[(RememberId,String,String)]= try {
    content.split(":").toList match {
      case List(id,seriesToken,rememberToken) => Some((ev.fromString(id),seriesToken,rememberToken))
      case _ => None
    }
  } catch {
    case scala.util.control.NonFatal(_) => None
  }

  implicit def rememberMeFromCookie(cookie: HttpCookie): Option[RememberMeCookie[RememberId]] =
    decode(cookie.content).map({case (id,seriesToken,rememberToken) =>
        RememberMeCookie(id,seriesToken,rememberToken,cookie.expires, cookie.maxAge, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extension)
    })


  implicit def rememberMeToCookie(remember: RememberMeCookie[RememberId]) : HttpCookie =
    HttpCookie(SiteSettings.RememberCookieName, encode(remember.id,remember.seriesToken,remember.rememberToken), remember.expires, remember.maxAge, remember.domain, remember.path, remember.secure, remember.httpOnly, remember.extension)


}



