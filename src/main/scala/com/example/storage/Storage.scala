package com.example
package storage


import slick.driver.ExtendedProfile
import slick.session.Database

trait Storage extends AccountStorage {
  self: Storage with Profile =>

  import profile.simple._
  implicit val session: Session

  val ddl = Accounts.ddl

  def createDB(implicit session:  Session): Unit = {
    try { ddl.create } catch {
      case e: Exception =>
    }
  }

  def dropDB(implicit session: Session): Unit = {
    try { ddl.drop } catch {
      case e: Exception =>

    }
  }

  def truncateDB(implicit session: Session) = { dropDB; createDB }

}



class Store(override val profile:ExtendedProfile,database: Database) extends Storage with Profile {
  implicit val session = database.createSession
}
