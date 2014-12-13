package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement

object Application extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction { implicit req =>

    Ok(views.html.index(loggedIn))
  }

}