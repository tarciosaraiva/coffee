package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.filters.csrf._

object Application extends Controller {

  val searchForm = Form("term" -> text.verifying("Please enter a search term.", {
    !_.grouped(2).isEmpty
  }))

  implicit val settings: Seq[Setting] = Setting.all

  def home = CSRFAddToken {
    Action {
      implicit request =>
        Ok(views.html.index(searchForm, Client.indexes, List.empty))
    }
  }

  def search = CSRFCheck {
    Action {
      implicit request =>
        searchForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.index(formWithErrors, Client.indexes, List.empty)),
          term => Ok(views.html.index(searchForm, Client.indexes, Client.findByName(term)))
        )
    }
  }

  def index(index: String) = CSRFAddToken {
    Action {
      implicit request =>
        Ok(views.html.index(searchForm, Client.indexes, Client.indexes(index)))
    }
  }

}
