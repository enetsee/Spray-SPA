package com.example.domain.ajax

import spray.json.{JsonFormat, DefaultJsonProtocol}

case class AjaxResult[+T](success:Boolean,content:Option[T],redirect:Option[String],errors:List[String])

object AjaxResult extends AjaxResultJsonProtocol

trait AjaxResultJsonProtocol extends DefaultJsonProtocol  {
  implicit def ajaxResultFormat[T:JsonFormat] = jsonFormat4(AjaxResult.apply[T])
}

