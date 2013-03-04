package com.example
package cookies

import spray.http.DateTime
import com.example.SiteSettings


case class SessionCookie(
                          data: Map[String, String] = Map.empty[String, String],
                          expires: Option[DateTime] = Some( DateTime.now + (SiteSettings.SessionCookieMaxAge * 1000) ),
                          maxAge: Option[Long] = Some( SiteSettings.SessionCookieMaxAge ),
                          domain: Option[String] = None,
                          path: Option[String] = None,
                          secure: Boolean = false,
                          httpOnly: Boolean = true,
                          extension: Option[String] = None) {

  def get(key: String) = data.get(key)
  def isEmpty: Boolean = data.isEmpty
  def +(kv: (String, String)) = copy(data + kv)
  def -(key: String) = copy(data - key)
  def apply(key: String) = data(key)

}









