package com.example
package domain


import shapeless._
import TypeOperators._
import spray.json.{SerializationException, JsString, JsValue, RootJsonFormat}
import slick.lifted.MappedTypeMapper


object NameModule {

  type Name = Newtype[String, NameOps]

  // TODO: add validation in the constructor
  def Name(s : String) : Name = newtype(s)

  case class NameOps(s : String) {
    def size : Int = s.size
  }


  implicit val mkNameOps = NameOps


  implicit object nameFormat extends RootJsonFormat[Name] {
    def read(value:JsValue) = value match  {
      case JsString(name) => Name(name)
      case _ => throw new SerializationException("Name: expected a string.")
    }

    def write(name:Name) = JsString(name.toString)
  }



  implicit val nameTypeMapper = MappedTypeMapper.base[Name, String](
  { case name : Name => name.toString },
  { case name : String => Name(name) })

}







object EmailModule {
  type Email = Newtype[String, EmailOps]

  // TODO: add validation in the constructor
  def Email(s : String) : Email = newtype(s.toLowerCase)

  case class EmailOps(s : String) {
    def size : Int = s.size
  }

  implicit val mkEmailOps = EmailOps

  implicit object emailFormat extends RootJsonFormat[Email] {
    def read(value:JsValue) = value match  {
      case JsString(email) => Email(email)
      case _ => throw new SerializationException("Email: expected a string.")
    }

    def write(email:Email) = JsString(email.toString)
  }


  implicit val emailTypeMapper = MappedTypeMapper.base[Email, String](
  { case email : Email => email.toString },
  { case email : String => Email(email) })

}