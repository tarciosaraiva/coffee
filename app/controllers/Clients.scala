package controllers

import play.api.mvc._
import play.api.libs.json.Json
import models._
import org.joda.time._
import play.api.data.Form
import play.api.data.Forms._
import scala.Predef._
import anorm.Id
import play.filters.csrf.{CSRFCheck, CSRFAddToken}

object Clients extends Controller {

  val transactionForm = Form(
    tuple(
      "coffee" -> text,
      "milk" -> text,
      "amount" -> bigDecimal,
      "credit" -> boolean
    ))

  val createForm = Form(tuple(
    "name" -> nonEmptyText,
    "balance" -> bigDecimal
      .verifying("Initial balance must be greater than A$25.", {
      _.>=(BigDecimal(25))
    }),
    "coffee" -> nonEmptyText,
    "milk" -> nonEmptyText,
    "addTransaction" -> boolean,
    "goToCustomerRecord" -> boolean
  ))

  def show(id: Long) = CSRFAddToken {
    Action {
      implicit request =>
        Ok(views.html.client(transactionForm, Client.findOne(id).get, Transaction.allByClient(id)))
    }
  }

  def transaction(id: Long) = CSRFCheck {
    Action {
      implicit request =>
        transactionForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.client(formWithErrors, Client.findOne(id).get, Transaction.allByClient(id))),
          trans => {
            Transaction.create(Transaction(DateTime.now, trans._4, trans._3, trans._1, trans._2, id))
            Client.updateBalance(id)
            Redirect(routes.Clients.show(id)).flashing(("success", "Transaction added successfully."))
          }
        )
    }
  }

  def create = CSRFCheck {
    Action {
      implicit request =>
        createForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest
          },
          client => {
            // add the client
            val newClientId = Client.create(Client(Id(1), client._1, BigDecimal(0), client._3, client._4)).get

            // add two transactions, one for the top-up
            Transaction.create(Transaction(DateTime.now, true, client._2, "", "", newClientId))

            // and the other for the coffee
            if (client._5)
              Transaction.create(Transaction(DateTime.now, false, BigDecimal(-3.4), client._3, client._4, newClientId))

            // and then updates the client
            Client.updateBalance(newClientId)

            if (client._6)
              Redirect(routes.Clients.show(newClientId))
            else
              Redirect(routes.Application.home).flashing(("success", "Client created successfully."))
          })
    }
  }

  def all = Action {
    Ok(Json.toJson(Client.all.map {
      c => c.toJson
    }))
  }

}
