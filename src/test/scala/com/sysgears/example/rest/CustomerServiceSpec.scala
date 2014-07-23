package com.sysgears.example.rest

import spray.http.HttpEntity
import net.liftweb.json.Serialization
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import com.sysgears.example.domain.Customer
//import the object with test data:
import com.sysgears.example.rest.CustomerTestData._
import com.sysgears.example.domain.CustomerConversions._
import spray.http.HttpRequest
import spray.routing.MalformedRequestContentRejection
import scala.Some

class CustomerServiceSpec extends CustomerServiceTestBase {
  //db for tests sets in application.conf
  //if the specified db does not exist, then you should create it.

  def before = cleanDB()

  "Customer service" should {
    "post customers" in {
      HttpRequest(POST, customerLink, entity = HttpEntity(
        Serialization.write(customers(0)))) ~> customerService ~> check {

        response.status should be equalTo Created
        response.entity should not be equalTo(None)
        val respCustomer = responseAs[Customer]
        respCustomer.id.get must be greaterThan 0
        respCustomer must be equalTo customers(0)
      }

      HttpRequest(POST, customerLink, entity = HttpEntity(
        Serialization.write(customers(1)))) ~> customerService ~> check {

        response.status should be equalTo Created
        response.entity should not be equalTo(None)
        val respCustomer = responseAs[Customer]
        respCustomer.id.get must be greaterThan 0
        respCustomer must be equalTo customers(1)
      }
    }

    "reject post requests without required data" in {
      "without any data" in {
        HttpRequest(POST, customerLink) ~> customerService ~> check {
          val rejectionMessage = rejections.head
            .asInstanceOf[MalformedRequestContentRejection].message
          rejectionMessage.contains("No usable value for firstName") === true
        }
      }

      "without lastName" in {
        HttpRequest(POST, customerLink,
          entity = HttpEntity( """{"firstName": ""}""")) ~> customerService ~> check {

          val rejectionMessage = rejections.head
            .asInstanceOf[MalformedRequestContentRejection].message
          rejectionMessage.contains("No usable value for lastName") === true
        }
      }
    }

    "return list of posted customers" in {
      Get(customerLink) ~> customerService ~> check {
        response.status should be equalTo OK
        response.entity should not be equalTo(None)
        val respCustomers = responseAs[List[Customer]]
        respCustomers.size should be equalTo 2
        respCustomers(0) must be equalTo customers(0)
        respCustomers(1) must be equalTo customers(1)
      }
    }

    "search for customers" in {
      "search by firstName" in {
        Get(s"$customerLink?firstName=$firstName0") ~> customerService ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val respCustomer = responseAs[Customer]
          respCustomer must be equalTo customers(0)
        }
      }

      "search by lastName" in {
        Get(s"$customerLink?lastName=$lastName1") ~> customerService ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val respCustomer = responseAs[Customer]
          respCustomer must be equalTo customers(1)
        }
      }

      "search by birthday" in {
        Get(s"$customerLink?birthday=${dateFormat.format(birthday1.get)}"
        ) ~> customerService ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val respCustomer = responseAs[Customer]
          respCustomer must be equalTo customers(1)
        }
      }

      "search by birthday and firstName" in {
        val params = s"birthday=${dateFormat.format(birthday0.get)}&firstName=$firstName0"
        Get(s"$customerLink?$params") ~> customerService ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val respCustomer = responseAs[Customer]
          respCustomer must be equalTo customers(0)
        }
      }

      "search for non-existent customer" in {
        Get(s"$customerLink?firstName=nonExistent") ~> customerService ~> check {
          response.status should be equalTo OK
          responseAs[List[Customer]].size === 0
        }
      }
    }

    "return customer by id" in {
      Get(s"$customerLink/${customersIds(0)}") ~> customerService ~> check {
        response.status should be equalTo OK
        response.entity should not be equalTo(None)
        responseAs[Customer] must be equalTo customers(0)
      }

      Get(s"$customerLink/${customersIds(1)}") ~> customerService ~> check {
        response.status should be equalTo OK
        response.entity should not be equalTo(None)
        responseAs[Customer] must be equalTo customers(1)
      }
    }

    "update customer data" in {
      HttpRequest(PUT, s"$customerLink/${customersIds(0)}", entity = HttpEntity(
        Serialization.write(customers(1)))) ~> customerService ~> check {
        response.status should be equalTo OK
        response.entity should not be equalTo(None)
        responseAs[Customer].copy(id = Some(customersIds(1))) must be equalTo customers(1)
      }

      Get(s"$customerLink/${customersIds(0)}") ~> customerService ~> check {
        response.status should be equalTo OK
        response.entity should not be equalTo(None)
        responseAs[Customer].copy(id = Some(customersIds(1))) must be equalTo customers(1)
      }
    }

    "reject put requests without required data" in {
      "without any data" in {
        HttpRequest(PUT, s"$customerLink/${customersIds(0)}"
        ) ~> customerService ~> check {
          val rejectionMessage = rejections.head
            .asInstanceOf[MalformedRequestContentRejection].message
          rejectionMessage.contains("No usable value for firstName") === true
        }
      }

      "without lastName" in {
        HttpRequest(PUT, s"$customerLink/${customersIds(1)}",
          entity = HttpEntity( """{"firstName": ""}""")) ~> customerService ~> check {

          val rejectionMessage = rejections.head
            .asInstanceOf[MalformedRequestContentRejection].message
          rejectionMessage.contains("No usable value for lastName") === true
        }
      }
    }

    "return 404 when we try to update non-existent customer" in {
      HttpRequest(PUT, s"$customerLink/$nonExistentCustomerId", entity = HttpEntity(
        Serialization.write(customers(1)))) ~> customerService ~> check {
        response.status should be equalTo NotFound
        response.entity should not be equalTo(None)
        responseAs[Map[String, String]].get("error") ===
          Some(s"Customer with id=$nonExistentCustomerId does not exist")
      }
    }

    "return 404 and error message when we try to get non-existent customer" in {
      Get(s"$customerLink/$nonExistentCustomerId") ~> customerService ~> check {
        response.status should be equalTo NotFound
        response.entity should not be equalTo(None)
        responseAs[Map[String, String]].get("error") ===
          Some(s"Customer with id=$nonExistentCustomerId does not exist")
      }
    }

    "delete created customers by id" in {
      customersIds.map {
        id =>
          Delete(s"$customerLink/$id") ~> customerService ~> check {
            response.status should be equalTo OK
            response.entity should not be equalTo(None)
          }
      }.find(!_.isSuccess) === None
    }

    "return 404 and error message when we try to delete non-existent customer" in {
      customersIds.map {
        id =>
          Delete(s"$customerLink/$id") ~> customerService ~> check {
            response.status should be equalTo NotFound
            response.entity should not be equalTo(None)
            responseAs[Map[String, String]].get("error") ===
              Some(s"Customer with id=$id does not exist")
          }
      }.find(!_.isSuccess) === None
    }
  }
}