package controllers

import models._
import org.joda.time._
import scala.Predef._
import utils.Utils._

import controllers.security.Secured
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm.Id

object Clients extends Controller with Secured {

  val searchForm = Form(tuple("term" -> text.verifying("Please enter a search term.", {
    !_.grouped(2).isEmpty
  }), "showHidden" -> boolean))

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

  val editForm = Form(tuple(
    "name" -> nonEmptyText,
    "email" -> optional(text),
    "twitter" -> optional(text),
    "dob" -> optional(jodaLocalDate("ddMM"))
  ))

  implicit val settings: Seq[Setting] = Setting.all

  implicit def indexes: Seq[String] = Client.indexes

  def home = IsAuthenticated {
    user => implicit request =>
      Ok(views.html.index(searchForm, "", List.empty))
  }

  def search = Action {
    implicit request =>
      searchForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.index(formWithErrors, "", List.empty)),
        frm => Ok(views.html.index(searchForm, "", Client.findByName(frm._1, frm._2)))
      )
  }

  def index(index: String) = IsAuthenticated {
    user => implicit request =>
      Ok(views.html.index(searchForm, index, Client.indexes(index)))
  }

  def show(id: Long) = IsAuthenticated {
    user => implicit request =>
      Ok(views.html.client(transactionForm, Client.findOne(id).get, Transaction.allByClient(id)))
  }

  def transaction(id: Long) = IsAuthenticated {
    user => implicit request =>
      transactionForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.client(formWithErrors, Client.findOne(id).get, Transaction.allByClient(id))),
        trans => {
          val amount = if (trans._3) trans._2 else trans._2.unary_-
          addTransaction(id, Seq(Transaction(Id(1), DateTime.now, trans._3, amount, trans._1, id)))

          if (trans._3) Client.markNotified(id)

          if (request.headers("Referer").contains("clients")) {
            Redirect(routes.Clients.show(id)).flashing(("success", "Transaction added successfully."))
          } else {
            Redirect(routes.Clients.home).flashing(("success", "Transaction added successfully."))
          }
        }
      )
  }

  def create = IsAuthenticated {
    user => implicit request =>
      createForm.bindFromRequest.fold(
        formWithErrors => {
          Redirect(routes.Clients.home).flashing(("error", "Sorry, could not create client. Maybe you forgot to enter something?"))
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
              client._5,
              BigDecimal(0),
              false,
              false)
          ).get

          val currentTime = DateTime.now
          val creditTransaction = true

          // add two transactions, one for the top-up
          addTransaction(newClientId, Seq(Transaction(Id(1), currentTime, creditTransaction, client._2, Option.apply(null), newClientId)), !client._6)

          // and the other for the coffee
          if (client._6)
            addTransaction(newClientId, Seq(Transaction(Id(1), currentTime, !creditTransaction, BigDecimal(-3.4), Option.apply("Coffee"), newClientId)))

          if (client._7)
            Redirect(routes.Clients.show(newClientId))
          else
            Redirect(routes.Clients.home).flashing(("success", "Client created successfully."))
        })
  }

  def edit(id: Long) = IsAuthenticated {
    user => implicit request =>
      editForm.bindFromRequest.fold(
        formWithErrors => BadRequest,
        updatedClient => {
          Client.updateDetails(id, updatedClient)
          Redirect(routes.Clients.show(id)).flashing(("success", "Client updated successfully."))
        }
      )
  }

  def deleteTransaction(cid: Long, tid: Long) = IsAuthenticated {
    user => implicit request =>
      val total = Client.deleteTransaction(cid, tid)
      var flashMsg = ("error", "Could not delete transaction. Does it belong to this customer?")
      if (total == 1) {
        flashMsg = ("success", "Transaction deleted successfully.")
        Client.updateBalance(cid)
      }

      Redirect(routes.Clients.show(cid)).flashing(flashMsg)
  }

  def editTransaction(cid: Long, tid: Long) = IsAuthenticated {
    user => implicit request =>
      transactionForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.client(formWithErrors, Client.findOne(cid).get, Transaction.allByClient(cid))),
        trans => {
          Transaction.updateTransaction(tid, cid, trans._2, trans._1)
          Client.updateBalance(cid)
          Redirect(routes.Clients.show(cid)).flashing(("success", "Transaction updated successfully."))
        })
  }

  def topups = IsAuthenticated {
    user => implicit request =>
      Ok(views.html.topups(Client.findAllTopUps))
  }

  def toggle(cid: Long) = IsAuthenticated {
    user => implicit request =>
      Client.toggleVisibility(cid)
      Redirect(routes.Clients.show(cid)).flashing(("success", "Client successfully hidden."))
  }

}
