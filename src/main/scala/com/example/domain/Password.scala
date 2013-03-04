package com.example
package domain

import slick.lifted.MappedTypeMapper
import spray.json.DefaultJsonProtocol
import org.mindrot.jbcrypt.BCrypt


/// Password inspired by Snap framework: https://github.com/snapframework/snap/blob/master/src/Snap/Snaplet/Auth/Types.hs
sealed trait Password {
  import Password._
  def fold[T](clearText: String => T, encrypted: String => T): T

  override def toString = fold[String](clearText => clearText, hashed => hashed)

  override def equals(that:Any) = that match {
    case that: Password => verify(this,that)
    case  _ => false
  }
}




object Password extends PasswordJsonProtocol with PasswordTypeMapper {

  object ClearText {
    def apply(pw: String) = new Password {
      def fold[Z](clearText: String => Z, encrypted: String => Z): Z = clearText(pw)
    }

    def unapply(r: Password): Option[String] =
      r.fold[Option[String]](pw => Some(pw), _ => None)
  }

  object Encrypted {
    def apply(pwhash: String) = new Password {
      def fold[Z](clearText: String => Z, encrypted: String => Z): Z = encrypted(pwhash)
    }
    def unapply(r: Password): Option[String] =
      r.fold[Option[String]](_ => None, pwHash => Some(pwHash))

  }

  def encrypt(logRounds:Int) : Password => Password = {
    case pw@Encrypted(_) => pw
    case ClearText(pw) => Encrypted(makePassword(pw,logRounds))
  }

  def verify(pw1:Password,pw2:Password) = (pw1,pw2) match {
    case (ClearText(pw1),ClearText(pw2)) => pw1 == pw2
    case (Encrypted(pw1),Encrypted(pw2)) => pw1 == pw2
    case (ClearText(pw1),Encrypted(pw2)) => verifyPassword(pw1,pw2)
    case (Encrypted(pw2),ClearText(pw1)) => verifyPassword(pw1,pw2)
  }

  private def makePassword(pw:String,logRounds:Int) : String =
    BCrypt.hashpw(pw, BCrypt.gensalt(logRounds))

  private def verifyPassword(plaintext:String,hashed:String) : Boolean =
    BCrypt.checkpw(plaintext, hashed)

}


trait PasswordTypeMapper {
  import Password._
  // only allow encrypted passwords to be persisted
  implicit val passwordTypeMapper = MappedTypeMapper.base[Password, String](
  { case Encrypted(pwHash) => pwHash },
  { case pwHash => Encrypted(pwHash) })
}




trait PasswordJsonProtocol extends DefaultJsonProtocol {
  import Password._
  import spray.json._

  implicit object passwordJsonFormat extends RootJsonFormat[Password] {
    // only allow encrypred passwords to be serialized
    def write(password: Password): JsValue = password match {
      case Encrypted(password) => password.toJson
      case ClearText(_) => throw new SerializationException("Password: Cannot serialize a clear text password to JSON.")
    }

    def read(value: JsValue) = value match {
      case JsString(password) => ClearText(password)
      case _ => throw new SerializationException("Password: Excepted a string.")
    }
  }
}



