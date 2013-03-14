package com.example
package actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.CircuitBreakerOpenException
import spray.routing._
import spray.httpx.{SprayJsonSupport, TwirlSupport}
import spray.http.{HttpIp, MediaTypes, StatusCodes}


import com.example.directives.{SessionCookieDirectives, RememberMeCookieDirectives, CustomMiscDirectives}
import domain.AccountOps
import domain.NameModule._
import domain.EmailModule._
import domain.ajax._
import html._
import cookies.RememberMeCookie
import cookies.SessionCookie



class ServiceActor extends Actor with HttpServiceActor with TwirlSupport with SprayJsonSupport
  with CustomMiscDirectives with SessionCookieDirectives with Directives with RememberMeCookieDirectives with AccountOps
  with AjaxResultJsonProtocol with AjaxSignInJsonProtocol with AjaxSignUpJsonProtocol with AjaxUpdatePasswordJsonProtocol   {

  // There is almost certainly a better way of doing this:
  // RememberMeCookie[T] requires the RememberMeCookieDirectives trait
  // to be polymorphic over T with an implicit in scope
  type RememberId = Long
  implicit val ev = util.AsString.longAsString

  val storage = context.actorFor("../storage")
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
                  setSession(SessionCookie(data=Map(("id"-> account.id.get.toString)),path=Some("/"))) {                                         // Set session cookie
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

              (path("signup") & post) {                                                              // * ajax sign-up
                entity(as[AjaxSignUp]) { signUp =>
                  validateFuture(retrieveAccountByEmail(signUp.email).map(_.isEmpty) ,                    // check the email address does not already exist
                    "The email address you provided is already registered to another account." ) {
                    provideFuture(createAccount(signUp)) { account =>
                      setSession(SessionCookie(data=Map(("id"-> account.id.get.toString)),path=Some("/"))) {
                        complete(StatusCodes.Created,account)
                      }
                    }
                  }
                }

              } ~ (path("signin") & post) {                                                           // * ajax sign-in
                entity(as[AjaxSignIn]) { signIn =>
                  optionalClientIP { ipOpt =>
                    authenticate(authenticateAccount(retrieveAccountByEmail(signIn.email),signIn.password,ipOpt)) { account =>
                      setSession(SessionCookie(data=Map(("id"-> account.id.get.toString)),path=Some("/"))) {
                        conditional(signIn.rememberMe,
                          setRememberMe(                                                             // Set remember-me cookie *if*  it was set on the sign-in form
                            RememberMeCookie(id=account.id.get,
                            seriesToken=account.seriesToken.get,
                            rememberToken=account.rememberToken.get,
                            path=Some("/")))) {
                              complete (StatusCodes.OK, account)
                        }
                      }
                    }
                  }
                }
              } ~ (path("signout") & post ) {                                                        // * ajax sign-out
                (deleteSession(path="/") & deleteRememberMe(path="/")) {                             // Delete the session & remember me cookie
                      complete { AjaxResult(true, (None: Option[Int]),Some("/signin"),List.empty) }  // Redirect to somewhere else...
                }
              }  ~ pathPrefix("account") {
                sessionCookie { session =>
                  val accountId = session("id").toLong


                  (path("") & get) {
                    complete (StatusCodes.OK, retrieveAccount(accountId) )
                  } ~ put {
                    path("name") {
                      entity(as[Name]) { newName =>
                        updateAccountName(accountId,newName)
                        complete  (StatusCodes.Accepted)
                      }
                    } ~ path("email") {
                      entity(as[Email]) { newEmail =>
                        updateAccountEmail(accountId,newEmail)
                        complete  (StatusCodes.Accepted)
                      }
                    } ~ path("password") {
                      entity(as[AjaxUpdatePassword]) { cp =>
                        authenticate(authenticateAccount(retrieveAccount(accountId),cp.password,None) ) { account =>
                          updateAccountPassword(accountId,cp.newPassword)
                          complete  (StatusCodes.Accepted)
                        }
                      }
                    }
                  }


                }
              }
            }
          }
        }
      }
  }


  val ajaxRejectionHandler =
    RejectionHandler.fromPF {
      case AuthenticationFailedRejection(msg)::_ =>
        complete(StatusCodes.Unauthorized,AjaxResult(false,None: Option[Int],None,List(msg)))

      case ls if ls.exists({ case ValidationRejection(_) => true}) =>
        val msgs = ls.collect({case ValidationRejection(ls) => ls})
        complete(StatusCodes.BadRequest, AjaxResult(false,None: Option[Int],None, msgs))


    }

  def ajaxExceptionHandler =
      ExceptionHandler.fromPF {

        case _ : CircuitBreakerOpenException =>
          // Circuit breaker has flipped; additional requests will fail fast - let the user know they should probably wait..
          complete(StatusCodes.ServiceUnavailable , AjaxResult(false,None:Option[Int],None,List("We are experiencing a temporary problem with our servers; please wait and try again in a few minutes.")))

        case _: Throwable =>
          complete(StatusCodes.InternalServerError,AjaxResult(false,None:Option[Int],None,List("There is a problem with our servers; please wait and try again in a few minutes.")))
      }


}


