package com.sysgears.example.rest

import java.text.SimpleDateFormat
import com.sysgears.example.domain.Customer

object CustomerTestData {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  //test data
  val birthday0 = Some(dateFormat.parse("1991-01-14"))
  val firstName0 = "Andrey"
  val lastName0 = "Litvinenko"
  val birthday1 = Some(dateFormat.parse("1987-01-14"))
  val firstName1 = "Corwin"
  val lastName1 = "Holmes"
  val customersIds = List(1, 2)
  val customers = List(Customer(Some(customersIds(0)), firstName0, lastName0, birthday0),
    Customer(Some(customersIds(1)), firstName1, lastName1, birthday1))
  val nonExistentCustomerId = customers.size + 1
}
