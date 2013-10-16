package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.filters.csrf._
import play.api.libs.functional.syntax._

object Api extends Controller {

  implicit val searchReads = (__ \ 'term).read[String]

  implicit val addClientReads = (
    (__ \ 'name).read[String] and
      (__ \ 'balance).read[BigDecimal] and
      (__ \ 'coffee).read[String] and
      (__ \ 'milk).read[String] and
      (__ \ 'addTransaction).read[Boolean] and
      (__ \ 'goToCustomerRecord).read[Boolean]
    ) tupled

  implicit val addTransactionReads = (
    (__ \ 'amount).read[BigDecimal] and
      (__ \ 'coffee).read[String] and
      (__ \ 'milk).read[String] and
      (__ \ 'credit).read[Boolean]
    ) tupled

  def search = CSRFCheck {
    Action(parse.json) {
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
  }

  def indexes = CSRFCheck {
    Action {
      request =>
        val result = Client.indexes
        Ok(Json.obj("__total" -> result.size, "data" -> result))
    }
  }

  def index(index: String) = CSRFCheck {
    Action {
      request =>
        val result = Client.indexes(index)
        Ok(Json.obj("__total" -> result.size, "data" -> result.map(_.toJson)))
    }
  }

  def client(id: Long) = CSRFCheck {
    Action {
      request =>
        val client = Client.findOne(id).get
        val transactions = Transaction.allByClient(id)
        Ok(Json.obj("client" -> client.toJson, "transactions" -> transactions.map(_.toJson)))
    }
  }

  def addClient = CSRFCheck {
    Action(parse.json) {
      request =>
        request.body.validate[(String, BigDecimal, String, String, Boolean, Boolean)].map {
          case (name, balance, coffee, milk, addTransaction, goToCustomerRecord) => {
            Ok(Json.obj("__total" -> 0))
          }
        }.recoverTotal {
          e => BadRequest(Json.obj("status" -> "NOK", "message" -> JsError.toFlatJson(e)))
        }
    }
  }

  def addTransaction(id: Long) = CSRFCheck {
    Action(parse.json) {
      request =>
        request.body.validate[(BigDecimal, String, String, Boolean)].map {
          case (amount, coffee, milk, credit) => {
            Ok(Json.obj("__total" -> 0))
          }
        }.recoverTotal {
          e => BadRequest(Json.obj("status" -> "NOK", "message" -> JsError.toFlatJson(e)))
        }
    }
  }

}
