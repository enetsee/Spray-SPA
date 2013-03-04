package com.example
package cookies

import spray.http.DateTime
import com.example.SiteSettings
import com.example.util.AsString

// See http://jaspan.com/improved_persistent_login_cookie_best_practice
case class RememberMeCookie[T](
                              id: T,
                              seriesToken: String,
                              rememberToken: String,
                              expires: Option[DateTime] = Some( DateTime.now + (SiteSettings.RememberCookieMaxAge * 1000) ),
                              maxAge: Option[Long] = Some( SiteSettings.RememberCookieMaxAge ),
                              domain: Option[String] = None,
                              path: Option[String] = None,
                              secure: Boolean = false,
                              httpOnly: Boolean = true,
                              extension: Option[String] = None)(implicit ev:AsString[T])


