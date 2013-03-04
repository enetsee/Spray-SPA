package com.example
package util

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import com.typesafe.config.ConfigFactory


trait Crypto {


    def sign(message: String, key: Array[Byte]): String = {
      val mac = Mac.getInstance("HmacSHA1")
      mac.init(new SecretKeySpec(key, "HmacSHA1"))
      Codecs.toHexString(mac.doFinal(message.getBytes("utf-8")))
    }

    def sign(message: String): String =
      sign(message, SiteSettings.ApplicationSecret.getBytes("utf-8"))

}


object Crypto extends Crypto
