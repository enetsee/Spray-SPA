package com.example
package domain

import spray.http.DateTime
import slick.lifted.MappedTypeMapper
import java.sql.Timestamp


// Slick mapping from spray DateTime to sql.TimeStamp
trait DateTimeTypeMapper {
    implicit val dateTimeTypeMapper = MappedTypeMapper.base[DateTime, Timestamp ](
    { case (dt: DateTime) => new  Timestamp(dt.clicks ) },
    { case (dt: Timestamp) => DateTime(dt.getTime()) })
}
