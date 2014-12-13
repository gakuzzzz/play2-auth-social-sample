package models.account

import scalikejdbc._

case class OpenIdAccount(id: OpenIdIdentifier, provider: OpenIdProvider, accountId: AccountId, name: String) {

}
object OpenIdAccount extends SQLSyntaxSupport[OpenIdAccount] {

  def apply(s: SyntaxProvider[OpenIdAccount])(rs: WrappedResultSet): OpenIdAccount = autoConstruct(rs, s)

  private val o = OpenIdAccount.syntax("t")
  private val oc = OpenIdAccount.column

  def findById(id: OpenIdIdentifier)(implicit session: DBSession): Option[OpenIdAccount] = {
    withSQL {
      select
        .from(OpenIdAccount as o)
        .where.eq(o.id, id)
    }.map(OpenIdAccount(o)).first().apply()
  }

  def insert(id: OpenIdIdentifier, provider: OpenIdProvider, accountId: AccountId, nickname: String)(implicit session: DBSession): OpenIdAccount = {
    applyUpdate {
      QueryDSL.insert.into(OpenIdAccount).namedValues(
        oc.id         -> id,
        oc.provider   -> provider.openId,
        oc.accountId  -> accountId,
        oc.name       -> nickname
      )
    }
    OpenIdAccount(id, provider, accountId, nickname)
  }

}