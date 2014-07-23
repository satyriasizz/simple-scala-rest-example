package com.sysgears.example.domain

import scala.slick.driver.MySQLDriver.simple._
import spray.http.{HttpCharsets, HttpEntity}
import net.liftweb.json.{DateFormat, Formats, Serialization}
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Customer entity.
 *
 * @param id        unique id
 * @param firstName first name
 * @param lastName  last name
 * @param birthday  date of birth
 */
case class Customer(id: Option[Long], firstName: String, lastName: String, birthday: Option[java.util.Date])

/**
 * Mapped customers table object.
 */
object Customers extends Table[Customer]("customers") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("first_name")

  def lastName = column[String]("last_name")

  def birthday = column[java.util.Date]("birthday", O.Nullable)

  def * = id.? ~ firstName ~ lastName ~ birthday.? <>(Customer, Customer.unapply _)

  implicit val dateTypeMapper = MappedTypeMapper.base[java.util.Date, java.sql.Date](
  {
    ud => new java.sql.Date(ud.getTime)
  }, {
    sd => new java.util.Date(sd.getTime)
  })

  val findById = for {
    id <- Parameters[Long]
    c <- this if c.id is id
  } yield c
}

object CustomerConversions {

  implicit val liftJsonFormats = new Formats {
    val dateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      def parse(s: String): Option[Date] = try {
        Some(sdf.parse(s))
      } catch {
        case e: Exception => None
      }

      def format(d: Date): String = sdf.format(d)
    }
  }

  implicit def HttpEntityToCustomer(httpEntity: HttpEntity) = Serialization.read[Customer](httpEntity.asString(HttpCharsets.`UTF-8`))
}