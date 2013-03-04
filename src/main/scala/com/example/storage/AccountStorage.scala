package com.example
package storage

import java.util.UUID
import spray.http.{HttpIp, DateTime}
import domain.{HttpIpTypeMapper, DateTimeTypeMapper, Password, Account}




trait AccountStorage extends DateTimeTypeMapper with HttpIpTypeMapper {
  self: AccountStorage with Profile =>

  import profile.simple._


  object Accounts extends Table[Account]("accounts") {
    
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)
    def email = column[String]("email", O.NotNull)

    def password = column[Password]("password", O.NotNull)

    def publicKey = column[String]("public_key", O.NotNull)
    def privateKey = column[String]("private_key", O.NotNull)

    def activatedAt = column[DateTime]("activated_at", O.Nullable)
    def suspendedAt = column[DateTime]("suspended_at", O.Nullable)

    def seriesToken = column[String]("series_token", O.Nullable)
    def rememberToken = column[String]("remember_token", O.Nullable)

    def loginCount = column[Int]("login_count", O.NotNull)
    def failedLoginCount = column[Int]("failed_login_count", O.NotNull)
    def lockedOutUntil = column[DateTime]("locked_out_until", O.Nullable)
    def currentLoginAt = column[DateTime]("current_login_at", O.Nullable)
    def lastLoginAt = column[DateTime]("last_login_at", O.Nullable)
    def currentLoginIp = column[HttpIp]("current_login_ip", O.Nullable)
    def lastLoginIp = column[HttpIp]("last_login_ip", O.Nullable)

    def createdAt = column[DateTime]("created_at", O.NotNull)
    def updatedAt = column[DateTime]("updated_at", O.Nullable)

    def resetToken = column[String]("reset_token", O.Nullable)
    def resetRequestedAt = column[DateTime]("reset_requested_at", O.Nullable)


    private[AccountStorage] def autoInc(implicit session: Session) =
      name ~ email ~ password ~ publicKey ~ privateKey ~ createdAt ~ seriesToken ~ rememberToken ~ loginCount ~ failedLoginCount returning id into {
        case (_, id) => id
      }


    def * =
      id.? ~ name ~ email ~ password ~
      publicKey.? ~ privateKey.? ~
      activatedAt.? ~ suspendedAt.? ~ seriesToken.? ~ rememberToken.? ~
      loginCount ~ failedLoginCount ~ lockedOutUntil.? ~
      currentLoginAt.? ~ lastLoginAt.? ~ currentLoginIp.? ~ lastLoginIp.? ~
      createdAt.? ~ updatedAt.? ~ resetToken.? ~ resetRequestedAt.? <> (Account.apply _ ,Account.unapply _ )

  }



  private def qRetrieveAccountByEmail(email: String)(implicit session: Session) =
    for { account <- Accounts if (account.email is email) } yield account

  private def qRetrieveAccountPassword(id: Long)(implicit session: Session) =
    for { account <- Accounts if (account.id is id) } yield account.password

  private def qRetrieveAccountKeys(id: Long)(implicit session: Session) =
    for { account <- Accounts if (account.id is id) } yield (account.publicKey ~ account.privateKey)

  private def qRetrieveAccount(id: Long)(implicit session: Session) =
    for { account <- Accounts if (account.id is id) } yield account

  private def qRetrieveAccountName(id: Long)(implicit session: Session) =
    for { account <- Accounts if (account.id is id) } yield account.name

  private def qRetrieveAccountEmail(id: Long)(implicit session: Session) =
    for { account <- Accounts if (account.id is id) } yield account.email




  def createAccount(account: Account)(implicit session: Session): Account = {
    val pw = Password.encrypt(SiteSettings.EncryptionLogRounds)(account.password)
    // set random values
    val publicKey = UUID.randomUUID().toString
    val privateKey = UUID.randomUUID().toString
    val rememberToken = UUID.randomUUID().toString
    val seriesToken = UUID.randomUUID().toString
    val createdAt = account.createdAt.getOrElse(DateTime.now)

    val id = Accounts.autoInc.insert(account.name, account.email.toLowerCase, pw, publicKey, privateKey, createdAt, seriesToken, rememberToken, 0, 0)
    account.copy(id = Some(id), password = pw, publicKey = Some(publicKey), privateKey=Some(privateKey),  createdAt = Some(createdAt))
  }


  def retrieveAccount(id: Long)(implicit session: Session) =
    qRetrieveAccount(id).firstOption

  def retrieveAccountPassword(id: Long)(implicit session: Session) =
    qRetrieveAccountPassword(id).firstOption

  def retrieveAccountByEmail(email: String)(implicit session: Session) =
    qRetrieveAccountByEmail(email.toLowerCase).firstOption



  def updateAccount(account: Account)(implicit session: Session) =
    Accounts.where(_.id is account.id.get).update(account)

  def updateAccountPassword(id: Long, password: Password)(implicit session: Session) =
    qRetrieveAccountPassword(id).update(Password.encrypt(SiteSettings.EncryptionLogRounds)(password))

  def updateAccountEmail(id: Long, email: String)(implicit session: Session) =
    qRetrieveAccountEmail(id).update(email)

  def updateAccountName(id: Long, login: String)(implicit session: Session) =
    qRetrieveAccountName(id).update(login)

  def deleteAccount(id: Long)(implicit session: Session) =
    qRetrieveAccount(id).delete


}
