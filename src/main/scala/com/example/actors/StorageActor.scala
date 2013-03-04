package com.example
package actors


import scala.concurrent.duration._

import akka.actor.{Actor,ActorLogging}
import akka.util.Timeout
import akka.pattern.CircuitBreaker

import com.example.storage.Storage
import com.example.domain.{Account, Password}

object StorageActor {
  trait StorageProtocolMessage

  case class CreateAccount(account: Account) extends StorageProtocolMessage // => Account

  case class RetrieveAccountByEmail(email: String) extends StorageProtocolMessage // => Option[Account]
  case class RetrieveAccount(accountId: Long) extends StorageProtocolMessage // => Option[Account]

  case class UpdateAccount(account: Account) extends StorageProtocolMessage // => Int
  case class UpdateAccountPassword(accountId: Long, newPassword: Password) extends StorageProtocolMessage // => Int
  case class UpdateAccountEmail(accountId: Long, newEmail: String) extends StorageProtocolMessage // => Int
  case class UpdateAccountName(accountId: Long, newName: String) extends StorageProtocolMessage // => Int


  case class DeleteAccount(accountId: Long) extends StorageProtocolMessage // => Int
}


class StorageActor(store: Storage) extends Actor with ActorLogging {
  import StorageActor._
  import context.dispatcher
  implicit val timeout: Timeout = 2.seconds

  import store._
  
  val breaker =
    new CircuitBreaker(context.system.scheduler,
      maxFailures = 5,
      callTimeout = 4.seconds,
      resetTimeout = 1.minute).onOpen(notifyMeOnOpen)

      
  def notifyMeOnOpen =
    log.warning("StorageActor: CircuitBreaker is now open, and will not close for one minute")

  def receive: Receive = {
    case CreateAccount(account) =>
      sender ! breaker.withSyncCircuitBreaker(createAccount(account))

    case DeleteAccount(accountId) =>
      sender ! breaker.withSyncCircuitBreaker(deleteAccount(accountId))

    case RetrieveAccount(id) =>
      sender ! breaker.withSyncCircuitBreaker(retrieveAccount(id))

    case RetrieveAccountByEmail(email) =>
      sender ! breaker.withSyncCircuitBreaker(retrieveAccountByEmail(email))

    case UpdateAccount(account) =>
      sender ! breaker.withSyncCircuitBreaker(updateAccount(account))

    case UpdateAccountPassword(accountId, newPassword) =>
      sender ! breaker.withSyncCircuitBreaker(updateAccountPassword(accountId, newPassword))


    case UpdateAccountEmail(accountId, newEmail) =>
      sender ! breaker.withSyncCircuitBreaker(updateAccountEmail(accountId, newEmail))

    case UpdateAccountName(accountId, newName) =>
      sender ! breaker.withSyncCircuitBreaker(updateAccountName(accountId, newName))

  }

}