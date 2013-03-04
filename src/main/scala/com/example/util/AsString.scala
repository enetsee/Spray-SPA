package com.example
package util


trait AsString[T] {
  def fromString(x:String): T
  def toString(x:T) : String = x.toString()
}

object AsString extends AsStringImplicitsLow


trait AsStringImplicitsLow {

  implicit val stringAsString = new AsString[String] {
    def fromString(x:String) = x
  }

  implicit val intAsString = new AsString[Int] {
    def fromString(x:String) = x.toInt
  }

  implicit val longAsString = new AsString[Long] {
    def fromString(x:String) = x.toLong
  }

}