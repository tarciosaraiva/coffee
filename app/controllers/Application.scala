package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Application extends Controller {

  implicit val settings: Seq[Setting] = Setting.all

  val loginForm = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying("Invalid username or password.", result => result match {
      case (username, password) => username.equals("bv") && password.equals("blackc0ff33")
    })
  )

  def login = Action {
    implicit request => Ok(views.html.login(loginForm))
  }

  def auth = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        user => Redirect(routes.Clients.home).withSession("user" -> user._1)
      )
  }

}
