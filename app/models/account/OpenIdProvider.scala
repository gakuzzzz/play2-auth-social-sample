package models.account

import _root_.play.api.mvc.QueryStringBindable
import scalikejdbc._

sealed abstract class OpenIdProvider(val openId: String, val nameAx: String)

object OpenIdProvider {

  case object Yahoo extends OpenIdProvider("https://me.yahoo.co.jp/", "http://axschema.org/namePerson/friendly")
  case object Mixi extends OpenIdProvider("https://mixi.jp/", "http://axschema.org/namePerson/friendly")

  val values: IndexedSeq[OpenIdProvider] = Vector(Yahoo, Mixi)

  def valueOf(value: String): Option[OpenIdProvider] = values.find(_.openId == value)

  implicit val typeBinder: TypeBinder[OpenIdProvider] = TypeBinder.string.map(v => valueOf(v).get)
  implicit val queryStringBindable: QueryStringBindable[OpenIdProvider] = new QueryStringBindable[OpenIdProvider] {
    override def unbind(key: String, value: OpenIdProvider): String = s"$key=$value"
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, OpenIdProvider]] = {
      implicitly[QueryStringBindable[String]].bind(key, params).map {
        _.right.flatMap { v =>
          values.find(_.toString == v).toRight(s"not found open id provider as $v")
        }
      }
    }
  }

}
