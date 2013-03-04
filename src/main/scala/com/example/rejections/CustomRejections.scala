package com.example
package rejections

import spray.routing.Rejection


trait CustomRejections {
  case object MissingSessionCookieRejection extends Rejection
  case object MissingRememberMeCookieRejection extends Rejection
}

object CustomRejections extends CustomRejections