package controllers

import models._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import controllers.security.Secured

object Settings extends Controller with Secured {

  val creationForm = Form(
    tuple(
      "key" -> nonEmptyText,
      "value" -> nonEmptyText
    ))

  implicit val settings: Seq[Setting] = Setting.all

  def show = Action {
    implicit request =>
      Ok(views.html.settings(creationForm, Setting.all))
  }

  def create = Action {
    implicit request =>
      creationForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.settings(creationForm, Setting.all)),
        setting => Redirect(routes.Settings.show)
      )
  }

  def update(id: Long) = Action {
    implicit request =>
      creationForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.settings(creationForm, Setting.all)),
        setting => Redirect(routes.Settings.show)
      )
  }

  def delete(id: Long) = Action {
    implicit request => {
      Setting.delete(id)
      Redirect(routes.Settings.show)
    }
  }

}
