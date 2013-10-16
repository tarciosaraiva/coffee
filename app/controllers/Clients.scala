package controllers

import play.api.mvc._
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
      "notes" -> text,
      "amount" -> bigDecimal,
      "credit" -> boolean
    ))

  val createForm = Form(tuple(
    "name" -> nonEmptyText,
    "balance" -> bigDecimal
      .verifying("Initial balance must be greater than A$25.", {
      _.>=(BigDecimal(25))
    }),
    "email" -> text,
    "twitter" -> text,
    "dob" -> jodaLocalDate("dd-MM"),
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
            val newClientId = Client.create(Client(Id(1), client._1, BigDecimal(0), client._3, client._4, client._5)).get

            // add two transactions, one for the top-up
            Transaction.create(Transaction(DateTime.now, true, client._2, "", newClientId))

            // and the other for the coffee
            if (client._6)
              Transaction.create(Transaction(DateTime.now, false, BigDecimal(-3.4), "Coffee", newClientId))

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
