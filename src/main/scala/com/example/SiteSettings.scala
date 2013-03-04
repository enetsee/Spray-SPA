package com.example

import spray.util.ConfigUtils
import com.typesafe.config.ConfigFactory


object SiteSettings {

  private val c = ConfigUtils.prepareSubConfig(ConfigFactory.load(), "spray.site")

  val Interface    = c getString  "interface"
  val Port         = c getInt     "port"
  val DevMode      = c getBoolean "dev-mode"

  val ApplicationSecret = c getString "application-secret"

  val SessionCookieName = c getString "session-cookie-name"
  val SessionCookieMaxAge = c getLong "session-cookie-max-age"

  val RememberCookieName = c getString "remember-cookie-name"
  val RememberCookieMaxAge = c getLong "remember-cookie-max-age"

  val EncryptionLogRounds = c getInt "encryption-log-rounds"

  val AccountLockout =  c getBoolean "account-lockout"
  val AccountLockoutMaxAttempts = c getInt "account-lockout-max-attempts"
  val AccountLockoutPeriod = c getLong "account-lockout-period"


  require(ApplicationSecret.nonEmpty,"application-secret must be non-empty")
  require(SessionCookieName.nonEmpty,"session-cookie-name must be non-empty")
  require(RememberCookieName.nonEmpty,"remember-cookie-name must be non-empty")
  require(0 < EncryptionLogRounds && EncryptionLogRounds < 16,"encyrption log-rounds must be between 1 and 15.")
  require(Interface.nonEmpty, "interface must be non-empty")
  require(0 < Port && Port < 65536, "illegal port")


}
