package controllers

import play.api.mvc._
import play.api.libs.json.Json
import models._
import org.joda.time._
import play.api.data.Form
import play.api.data.Forms._
import scala.Predef._
import play.api.data.validation.Constraints._
import anorm.Id

object Clients extends Controller {

  val creditForm = Form("amount" -> text.verifying(nonEmpty))

  val debitForm = Form(tuple(
    "coffee" -> nonEmptyText,
    "milk" -> nonEmptyText
  ))

  val createForm = Form(tuple(
    "name" -> nonEmptyText,
    "balance" -> number(min = 25),
    "coffee" -> nonEmptyText,
    "milk" -> nonEmptyText
  ))

  def show(id: Long) = Action {
    implicit request =>
      Ok(views.html.client(creditForm, debitForm, Client.findOne(id).get, Transaction.allByClient(id)))
  }

  def credit(id: Long) = Action {
    implicit request =>
      creditForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.client(formWithErrors, debitForm, Client.findOne(id).get, Transaction.allByClient(id))),
        amount => {
          Transaction.create(Transaction(DateTime.now, true, BigDecimal(amount), "", "", id))
          Client.updateBalance(id)
          Redirect(routes.Clients.show(id)).flashing(("message", "Credit applied!"))
        })
  }

  def debit(id: Long) = Action {
    implicit request =>
      debitForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.client(creditForm, formWithErrors, Client.findOne(id).get, Transaction.allByClient(id))),
        debit => {
          Transaction.create(Transaction(DateTime.now, false, BigDecimal(-3.4), debit._1, debit._2, id))
          Client.updateBalance(id)
          Redirect(routes.Clients.show(id)).flashing(("message", "Debit applied!"))
        })
  }

  def create = Action {
    implicit request =>
      createForm.bindFromRequest.fold(
        formWithErrors => BadRequest,
        client => {
          Client.create(Client(Id(1), client._1, BigDecimal(client._2), client._3, client._4))
          Redirect(routes.Application.home)
        })
  }

  def all = Action {
    Ok(Json.toJson(Client.all.map {
      c => c.toJson
    }))
  }

}
