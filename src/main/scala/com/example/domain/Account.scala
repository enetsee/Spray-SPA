package com.example
package domain

import concurrent.Future
import concurrent.duration._

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout

import spray.http.{HttpIp, DateTime}
import spray.json._
import spray.json.{ JsObject, JsString, JsNumber }
import spray.routing.authentication._
import spray.routing.{AuthenticationFailedRejection, ValidationRejection, AuthorizationFailedRejection}

import actors.{ServiceActor, StorageActor}
import cookies.RememberMeCookie
import NameModule.Name
import EmailModule.Email

/// Account type inspired by Snap framework: https://github.com/snapframework/snap/blob/master/src/Snap/Snaplet/Auth/Types.hs
case class Account(
  id: Option[Long] = None, name: Name, email: Email                                         // identity
  , password: Password                                                                      // password (stored encrypted)
  , publicKey: Option[String] = None,privateKey: Option[String] = None                      // public & private keys
  , activatedAt: Option[DateTime] = None, suspendedAt: Option[DateTime] = None              // account activation / suspension
  , seriesToken: Option[String] = None, rememberToken: Option[String] = None                // remember me (http://jaspan.com/improved_persistent_login_cookie_best_practice)
  , loginCount: Int = 0, failedLoginCount: Int = 0, lockedOutUntil: Option[DateTime] = None // login failure
  , currentLoginAt: Option[DateTime] = None, lastLoginAt: Option[DateTime] = None           // login time
  , currentLoginIp: Option[HttpIp] = None, lastLoginIp: Option[HttpIp] = None               // login IP address
  , createdAt: Option[DateTime] = None, updatedAt: Option[DateTime] = None                  // record change
  , resetToken: Option[String] = None, resetRequestedAt: Option[DateTime] = None            // password reset
  )

object Account extends AccountJsonProtocol



trait AccountOps {
  self: ServiceActor with AccountOps =>

  implicit val timeout: Timeout = 2.seconds

  def createAccount(account:Account) : Future[Account] =
    ask(storage,StorageActor.CreateAccount(account)).mapTo[Account]

  def retrieveAccount(accountId:Long) : Future[Option[Account]] =
    ask(storage,StorageActor.RetrieveAccount(accountId)).mapTo[Option[Account]]

  def retrieveAccountByEmail(email:Email) : Future[Option[Account]] =
    ask(storage,StorageActor.RetrieveAccountByEmail(email)).mapTo[Option[Account]]

  def updateAccount(account:Account) : Future[Boolean] =
    ask(storage,StorageActor.UpdateAccount(account)).mapTo[Int].map({ case 0 => false case _ => true})

  def updateAccountName(accountId:Long,newName:Name) : Future[Boolean] =
    ask(storage,StorageActor.UpdateAccountName(accountId,newName)).mapTo[Int].map({ case 0 => false case _ => true})

  def updateAccountEmail(accountId:Long,newEmail:Email) : Future[Boolean] =
    ask(storage,StorageActor.UpdateAccountEmail(accountId,newEmail)).mapTo[Int].map({ case 0 => false case _ => true})

  def updateAccountPassword(accountId:Long,newPassword:Password) : Future[Boolean] =
    ask(storage,StorageActor.UpdateAccountPassword(accountId,newPassword)).mapTo[Int].map({ case 0 => false case _ => true})

  def deleteAccount(accountId:Long): Future[Boolean] =
    ask(storage,StorageActor.DeleteAccount(accountId)).mapTo[Int].map({ case 0 => false case _ => true})

  private def accountUnlocked(account:Account) = account.lockedOutUntil.map(_ <= DateTime.now).getOrElse(true)



  def authenticateAccount(accountOpt:Future[Option[Account]],password:Password,ipAddress:Option[HttpIp]) : Future[Authentication[Account]] =
    accountOpt.map({
      case Some(account) if accountUnlocked(account) =>

        if (Password.verify(password,account.password)) {
          val updatedAccount = updateAccountSignInSuccess(account,ipAddress)
          updateAccount( updatedAccount )
          Right(updatedAccount)
        } else {
          updateAccount( updateAccountSignInFailure(account,ipAddress)  )
          Left(AuthenticationFailedRejection("The password you provided is incorrect."))
        }

      case Some(_) => Left(AuthenticationFailedRejection("Your account is locked; please try again in a few moments."))
      case _ => Left(ValidationRejection("Email address not found."))
    })



  // Based on http://jaspan.com/improved_persistent_login_cookie_best_practice :
  // If the triplet is present, the user is considered authenticated.
  // The used token is removed from the database. A new token is generated, stored in database with the username and the same series identifier, and a new login cookie containing all three is issued to the user.
  // If the username and series are present but the token does not match, a theft is assumed.
  // The user receives a strongly worded warning and all of the user's remembered sessions are deleted.
  def authenticateRememberMe(remember:RememberMeCookie[Long],ipAddress:Option[HttpIp]) : Future[Authentication[Account]] =
    retrieveAccount(remember.id).map({
      case Some(account) =>
        (account.seriesToken.map(_ == remember.seriesToken).getOrElse(false) , account.rememberToken.map(_ == remember.rememberToken).getOrElse(false)) match {

          case (true,true) =>
            val updatedAccount = updateAccountRememberMeSuccess(account,ipAddress)
            updateAccount(updatedAccount)
            Right(updatedAccount)
          case (true,_) =>
            // id and series are present but remember token is wrong
            // this is where some other action would be taken to inform account holder of potential theft
            updateAccount(updateAccountRememberMeFailure(account,ipAddress,true))
            Left(AuthenticationFailedRejection("There is a problem with your persistent sign-in cookie."))

          case (_,_) =>
            // id is present but series and remember tokens are wrong
            updateAccount(updateAccountRememberMeFailure(account,ipAddress,false))
            Left(AuthenticationFailedRejection("There is a problem with your persistent sign-in cookie."))
        }

      case _ => Left(ValidationRejection("Email address not found."))
    })

  // Update account to reflect a successful sign in attempt
  def updateAccountSignInSuccess(account:Account,ipAddress:Option[HttpIp]) : Account = account.copy(
      loginCount = account.loginCount+1
      ,failedLoginCount = 0
      , lastLoginAt = account.currentLoginAt
      , currentLoginAt = Some(DateTime.now)
      , lastLoginIp = account.currentLoginIp
      , currentLoginIp = ipAddress
    )

  // Update account to reflect a failed sign in attempt
  def updateAccountSignInFailure(account:Account,ipAddress:Option[HttpIp]) : Account = account.copy(
    failedLoginCount = account.failedLoginCount+1
    , lockedOutUntil = if (SiteSettings.AccountLockout && SiteSettings.AccountLockoutMaxAttempts < account.failedLoginCount + 1) Some(DateTime.now + (SiteSettings.AccountLockoutPeriod * 1000)) else None
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
  )

  // Update account to reflect a successful sign in attempt using a remember-me cookie
  def updateAccountRememberMeSuccess(account:Account,ipAddress:Option[HttpIp]) : Account = account.copy(
    loginCount = account.loginCount+1
    , failedLoginCount = 0
    , lastLoginAt = account.currentLoginAt
    , currentLoginAt = Some(DateTime.now)
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
    , rememberToken =  Some(UUID.randomUUID().toString)
  )

  // Update account to reflect a failed sign in attempt using a remember-me cookie
  def updateAccountRememberMeFailure(account:Account,ipAddress:Option[HttpIp],lock:Boolean) : Account = account.copy(
    failedLoginCount = account.failedLoginCount+1
    , lockedOutUntil = if (lock || (SiteSettings.AccountLockout && SiteSettings.AccountLockoutMaxAttempts < account.failedLoginCount + 1)) Some(DateTime.now + (SiteSettings.AccountLockoutPeriod * 1000)) else None
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
    , seriesToken = Some(UUID.randomUUID().toString)
    , rememberToken =  Some(UUID.randomUUID().toString)
  )
}







trait AccountJsonProtocol extends DefaultJsonProtocol {
  import NameModule.nameFormat
  import EmailModule.emailFormat

  implicit object accountJsonFormat extends RootJsonFormat[Account] {

    def write(account: Account): JsValue = account.id match {
      case Some(id) =>
        JsObject(List(
          ("id" -> account.id.toJson),
          ("name" -> account.name.toJson),
          ("email" -> account.email.toJson)
        ))
      case None => throw new SerializationException("Account: cannot serialize an uninitialized account.")
    }

    def read(value: JsValue) = throw new SerializationException("Account: cannot deserialize an account.")

  }
}

