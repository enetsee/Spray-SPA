package com.example
package util

trait Codecs {
  private val hexChars = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')



  def toHex(array: Array[Byte]): Array[Char] = {

    val result = new Array[Char](array.length * 2)

    for (i <- 0 until array.length) {

      val b = array(i) & 0xff

      result(2 * i) = hexChars(b >> 4)

      result(2 * i + 1) = hexChars(b & 0xf)

    }

    result

  }



  def toHexString(array: Array[Byte]): String = {

    new String(toHex(array))

  }
}

object Codecs extends Codecs
