package com.example
package util


import scala.util.matching.Regex


trait Validation {

  private def isValid(regex:Regex): String => Boolean =
    regex.unapplySeq(_).isDefined

  def isValidEmail : String => Boolean =
    isValid("""(\w+)@([\w\.]+)""".r)

  def isValidStrongPassword: String => Boolean =
    isValid("""^.*(?=.{6,})(?=.*[a-z])(?=.*[A-Z])(?=.*[\d\W]).*$""".r)
}

object Validation extends Validation

