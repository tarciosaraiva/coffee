package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.filters.csrf.CSRFCheck

object Api extends Controller {

  implicit val searchReads = (__ \ 'term).read[String]

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
}
