package com.example
package domain

import slick.lifted.MappedTypeMapper
import spray.http.HttpIp

trait HttpIpTypeMapper {
  implicit val httpIpTypeMapper = MappedTypeMapper.base[HttpIp,String](
  { case (ip:HttpIp) => ip.value },
  { case (ip:String) => HttpIp(ip)}
  )
}

