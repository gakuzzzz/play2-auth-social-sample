package controllers

import jp.t2v.lab.play2.stackc.{RequestAttributeKey, RequestWithAttributes, StackableController}
import play.api.mvc.{Result, Controller}
import scala.concurrent.Future
import scalikejdbc._

trait TxElement extends StackableController { self: Controller =>

  private object DBSessionKey extends RequestAttributeKey[DBSession]

  override def proceed[A](request: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    import scalikejdbc.TxBoundary.Future._
    DB.localTx { session =>
      super.proceed(request.set(DBSessionKey, session))(f)
    }
  }

  implicit def dbSession(implicit req: RequestWithAttributes[_]): DBSession = req.get(DBSessionKey).get

}
