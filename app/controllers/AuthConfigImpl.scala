package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import models.account.{Account, AccountId}
import scala.reflect._
import scala.concurrent.{Future, ExecutionContext}
import scalikejdbc.AutoSession
import play.api.mvc.{Controller, Result, RequestHeader}

trait AuthConfigImpl extends AuthConfig { self: Controller =>

  type Id = AccountId
  type User = Account
  type Authority = Unit
  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future.successful(Account.findById(id)(AutoSession))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.SessionController.index))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.SessionController.index))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    true
  }

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

}
