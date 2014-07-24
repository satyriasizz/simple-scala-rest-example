package com.sysgears.example.rest

import scala.language.existentials
import org.specs2.mutable.{Before, Specification}
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import akka.actor.ActorRefFactory
import spray.http.{HttpCharsets, HttpEntity}
import net.liftweb.json.Serialization
import com.sysgears.example.domain.{Customers, Customer}
import com.sysgears.example.domain.CustomerConversions._
import com.sysgears.example.config.Configuration
import scala.slick.session.Database
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.driver.MySQLDriver.simple._
import slick.jdbc.meta.MTable


trait CustomerServiceTestBase extends Specification with Specs2RouteTest with HttpService with Configuration with Before {
  args(sequential = true) //makes test execution sequential

  val customerLink = "/customer"

  // connects the DSL to the test ActorSystem
  implicit def actorRefFactory = system

  val spec = this

  val customerService = new RestService {
    override implicit def actorRefFactory: ActorRefFactory = spec.actorRefFactory
  }.rest

  /**
   * Cleans the DB before tests.
   */
  def cleanDB() = {
    // init Database instance
    val db = Database.forURL(url = s"jdbc:mysql://$dbHost:$dbPort/$dbName",
      user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

    // drop tables if exist
    db.withSession {
      if (MTable.getTables("customers").list().nonEmpty) {
        Customers.ddl.drop
        Customers.ddl.create
      }
    }
  }

  // converts responses from the service
  implicit def HttpEntityToListOfCustomers(httpEntity: HttpEntity) = Serialization.read[List[Customer]](httpEntity.asString(HttpCharsets.`UTF-8`))

  implicit def HttpEntityToErrors(httpEntity: HttpEntity) = Serialization.read[Map[String, String]](httpEntity.asString(HttpCharsets.`UTF-8`))
}