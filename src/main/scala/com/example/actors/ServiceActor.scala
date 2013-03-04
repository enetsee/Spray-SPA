package com.example
package actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.CircuitBreakerOpenException
import spray.routing._
import spray.httpx.{SprayJsonSupport, TwirlSupport}
import spray.http.{HttpIp, MediaTypes, StatusCodes}


import com.example.directives.{SessionCookieDirectives, RememberMeCookieDirectives, CustomMiscDirectives}
import domain.{Account, AccountOps}
import domain.ajax.{AjaxResultJsonProtocol, AjaxResult}
import domain.Password.ClearText
import cookies.RememberMeCookie
import cookies.SessionCookie
import html._




class ServiceActor(val storage: ActorRef) extends Actor with HttpServiceActor with TwirlSupport with SprayJsonSupport
  with CustomMiscDirectives with SessionCookieDirectives with Directives with RememberMeCookieDirectives with AccountOps with AjaxResultJsonProtocol   {

  // There is almost certainly a better way of doing this:
  // RememberMeCookie[T] requires the RememberMeCookieDirectives trait
  // to be polymorphic over T with an implicit in scope
  type RememberId = Long
  implicit val ev = util.AsString.longAsString

  def receive = runRoute(route)




  def route = dynamicIf(SiteSettings.DevMode) {
      get {
          getFromResourceDirectory("theme") ~
          path("") {
            sessionCookie { session =>                                                              // Look for a session cookie
              complete{page(app())}
            } ~ rememberMeCookie { remember =>                                                       // No session cookie, look for a remember-me cookie
              optionalClientIP { ipOpt =>
                authenticate(authenticateRememberMe(remember,ipOpt)) { account =>                        // Authenticate sign-in attempt with remember-me cookie
                  setSession(SessionCookie(path=Some("/"))) {                                         // Set session cookie
                    setRememberMe(RememberMeCookie(id=account.id.get,                                 // Set a  new remember me cookie with rolled token
                      seriesToken=account.seriesToken.get,
                      rememberToken=account.rememberToken.get,path=Some("/"))){
                      complete{page(app())}
                    }
                  }
                }
              }
            } ~  (deleteRememberMe(path="/") & redirect("/signin",StatusCodes.TemporaryRedirect))  // No authenticated cookies - clean up and redirect to sign in

          } ~ path("signin") {
            complete{page(signin())}                                                                // apply page template to sign-in template
          } ~ path("signup") {
            complete{page(signup())}                                                                // apply page template to sign-up template
          }



      } ~  pathPrefix("ajax") {
        respondWithMediaType(MediaTypes.`application/json`) {
          handleRejections(ajaxRejectionHandler) {                                                   // wrap the inner route with rejection & exception
            handleExceptions(ajaxExceptionHandler) {                                                 // handler providing error messages in an 'AjaxResponse'
              (path("signup") & post) {                                                              // ajax sign-up
                formFields('name,'email,'password) {
                  (name,email,password) =>
                    validateFuture(retrieveAccountByEmail(email).map(_.isEmpty) ,                    // check the email address does not already exist
                      "Email address is already registered to another account." ) {
                        setSession(SessionCookie(path=Some("/"))) {
                          complete {
                            createAccount(Account(name=name,email=email,password=ClearText(password))). // Create account...
                            map(ac => AjaxResult(true,Some(ac.id.get),Some("/"),List.empty))            // since this the ajax route we don't return the resource
                          }
                        }
                    }
                }


            } ~ (path("signin") & post) {                                                          // ajax sign-in
              formFields('email,'password,'remember.as[Boolean]?) {
                (email,password,remember) =>
                  optionalClientIP { ipOpt =>
                    authenticate( authenticateAccount(email,ClearText(password),ipOpt)) { account =>
                      setSession(SessionCookie(path=Some("/"))) {                                    // Set session cookie
                        conditional(remember.getOrElse(false),
                          setRememberMe(                                                             // Set remember-me cookie *if*  it was set on the sign-in form
                            RememberMeCookie(id=account.id.get,
                            seriesToken=account.seriesToken.get,
                            rememberToken=account.rememberToken.get,
                            path=Some("/")))) {
                              complete { AjaxResult(true,Some(account.id.get),Some("/"),List.empty) }
                        }
                      }
                    }
                  }
               }


              } ~ (path("signout") & post ) {                                                        // sign-out
                (deleteSession(path="/") & deleteRememberMe(path="/")) {                             // Delete the session & remember me cookie
                      complete { AjaxResult(true, (None: Option[Int]),Some("/signin"),List.empty) }  // Redirect to somewhere else...
                }

              }
            }
          }
        }
      }
  }


  val ajaxRejectionHandler =
    RejectionHandler.fromPF {
      case AuthenticationFailedRejection(msg)::_ => complete { AjaxResult(false,None: Option[Int],None,List(msg))}
      case ls if ls.exists({ case ValidationRejection(_) => true}) =>
        val msgs = ls.collect({case ValidationRejection(ls) => ls})
        complete { AjaxResult(false,None: Option[Int],None, msgs)}
    }

  def ajaxExceptionHandler =
      ExceptionHandler.fromPF {

        case _ : CircuitBreakerOpenException =>
          // Circuit breaker has flipped; additional requests will fail fast - let the user know they should probably wait..
          complete { AjaxResult(false,None:Option[Int],None,
            List("We are experiencing a temporary problem with our servers; please wait and try again in a few minutes."))
          }

        case _: Throwable =>
          complete {
            AjaxResult(false,None:Option[Int],None,List("There is a problem with our servers; please wait and try again in a few minutes."))
          }
      }


}


