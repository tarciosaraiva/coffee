package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.{DateTime, LocalDate}
import anorm.Id

object Api extends Controller {

  implicit val searchReads = (__ \ 'term).read[String]

  implicit val addClientReads = (
    (__ \ 'name).read[String] and
      (__ \ 'balance).read[BigDecimal] and
      (__ \ 'email).read[Option[String]] and
      (__ \ 'twitter).read[Option[String]] and
      (__ \ 'dob).read[Option[LocalDate]] and
      (__ \ 'addTransaction).read[Boolean]
    ) tupled

  implicit val addTransactionReads = (
    (__ \ 'amount).read[BigDecimal] and
      (__ \ 'notes).read[Option[String]] and
      (__ \ 'credit).read[Boolean]
    ) tupled

  def search = Action(parse.json) {
    request =>
      request.body.validate[(String)].map {
        case (term) => {
          val result = Client.findByName(term)
          Ok(Json.obj("__total" -> result.size, "data" -> result.map(_.toJson)))
        }
      }.recoverTotal {
        e => BadRequest(Json.obj("status" -> "NOK", "message" -> JsError.toFlatJson(e)))
      }
  }

  def indexes = Action {
    request =>
      val result = Client.indexes
      Ok(Json.obj("__total" -> result.size, "data" -> result))
  }

  def index(index: String) = Action {
    request =>
      val result = Client.indexes(index)
      Ok(Json.obj("__total" -> result.size, "data" -> result.map(_.toJson)))
  }

  def client(id: Long) = Action {
    request =>
      val client = Client.findOne(id).get
      val transactions = Transaction.allByClient(id)
      Ok(Json.obj("client" -> client.toJson, "transactions" -> transactions.map(_.toJson)))
  }

  def addClient = Action(parse.json) {
    request =>
      request.body.validate[(String, BigDecimal, Option[String], Option[String], Option[LocalDate], Boolean)].map {
        case (name, balance, email, twitter, dob, addTransaction) => {
          // add the client
          val newClientId = Client.create(Client(Id(1), name, BigDecimal(0), email, twitter, dob)).get

          // add two transactions, one for the top-up
          Transaction.create(Transaction(DateTime.now, true, balance, null, newClientId))

          // and the other for the coffee
          if (addTransaction)
            Transaction.create(Transaction(DateTime.now, false, BigDecimal(-3.4), Option.apply("Coffee"), newClientId))

          // and then updates the client
          Client.updateBalance(newClientId)

          Ok(Json.obj("id" -> newClientId))
        }
      }.recoverTotal {
        e => BadRequest(Json.obj("status" -> "NOK", "message" -> JsError.toFlatJson(e)))
      }
  }

  def addTransaction(id: Long) = Action(parse.json) {
    request =>
      request.body.validate[(BigDecimal, Option[String], Boolean)].map {
        case (amount, notes, credit) => {
          Transaction.create(Transaction(DateTime.now, credit, amount, notes, id))
          Client.updateBalance(id)
          Ok
        }
      }.recoverTotal {
        e => BadRequest(Json.obj("status" -> "NOK", "message" -> JsError.toFlatJson(e)))
      }
  }

}
