package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Client

object Application extends Controller {

  val searchForm = Form("term" -> text.verifying("Please enter a search term.", {
    !_.grouped(2).isEmpty
  }))

  def home = Action {
    implicit request =>
      Ok(views.html.index(searchForm, Client.indexes, List.empty))
  }

  def search = Action {
    implicit request =>
      searchForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.index(formWithErrors, Client.indexes, List.empty)),
        term => Ok(views.html.index(searchForm, Client.indexes, Client.findByName(term)))
      )
  }

  def index(index: String) = Action {
    implicit request =>
      Ok(views.html.index(searchForm, Client.indexes, Client.indexes(index)))
  }

}
