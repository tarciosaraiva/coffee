package controllers

import play.api.mvc._
import models._
import org.joda.time._
import play.api.data.Form
import play.api.data.Forms._
import scala.Predef._
import anorm._
import play.filters.csrf._

object Clients extends Controller {

  val transactionForm = Form(
    tuple(
      "notes" -> optional(text),
      "amount" -> bigDecimal,
      "credit" -> boolean
    ))

  val createForm = Form(tuple(
    "name" -> nonEmptyText,
    "balance" -> bigDecimal,
    "email" -> optional(text),
    "twitter" -> optional(text),
    "dob" -> optional(jodaLocalDate("ddMM")),
    "addTransaction" -> boolean,
    "goToCustomerRecord" -> boolean
  ))

  implicit val settings: Seq[Setting] = Setting.all

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
            val amount = if (trans._3) trans._2 else trans._2.unary_-
            Transaction.create(Transaction(DateTime.now, trans._3, amount, trans._1, id))
            Client.updateBalance(id)

            if (request.headers("Referer").contains("clients")) {
              Redirect(routes.Clients.show(id)).flashing(("success", "Transaction added successfully."))
            } else {
              Redirect(routes.Application.home).flashing(("success", "Transaction added successfully."))
            }
          }
        )
    }
  }

  def create = CSRFCheck {
    Action {
      implicit request =>
        createForm.bindFromRequest.fold(
          formWithErrors => {
            Redirect(routes.Application.home).flashing(("error", "Sorry, could not create client. Maybe you forgot to enter something?"))
          },
          client => {
            // add the client
            val newClientId = Client.create(
              Client(
                Id(1),
                client._1,
                BigDecimal(0),
                client._3,
                client._4,
                client._5)
            ).get

            val currentTime = DateTime.now

            // add two transactions, one for the top-up
            Transaction.create(Transaction(currentTime, true, client._2, Option.apply(null), newClientId))

            // and the other for the coffee
            if (client._6)
              Transaction.create(Transaction(currentTime, false, BigDecimal(-3.4), Option.apply("Coffee"), newClientId))

            // and then updates the client
            Client.updateBalance(newClientId)

            if (client._7)
              Redirect(routes.Clients.show(newClientId))
            else
              Redirect(routes.Application.home).flashing(("success", "Client created successfully."))
          })
    }
  }

}
