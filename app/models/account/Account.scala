package models.account

import scalikejdbc._

case class Account(id: AccountId, name: String, twitter: Option[TwitterAccount] = None, openIds: Seq[OpenIdAccount] = Vector())

object Account extends SQLSyntaxSupport[Account] {

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = autoConstruct(rs, s, "twitter", "openIds")

  private val a = Account.syntax("a")
  private val t = TwitterAccount.syntax("t")
  private val o = OpenIdAccount.syntax("o")
  private val ac = Account.column

  def findById(id: AccountId)(implicit session: DBSession): Option[Account] = {
    withSQL {
      select
        .from(Account as a)
        .leftJoin(TwitterAccount as t).on(a.id, t.accountId)
        .leftJoin(OpenIdAccount as o).on(a.id, o.accountId)
      .where.eq(a.id, id)
    }.one(rs => Account(a)(rs).copy(twitter = rs.longOpt(t.resultName.id).map(_ => TwitterAccount(t)(rs))))
      .toMany(rs => rs.stringOpt(o.resultName.id).map(_ => OpenIdAccount(o)(rs)))
      .map((a, os) => a.copy(openIds = os))
      .first().apply()
  }

  def insert(name: String)(implicit session: DBSession): Account = {
    val id = applyUpdateAndReturnGeneratedKey {
      QueryDSL.insert.into(Account).namedValues(ac.name -> name)
    }
    Account(id.toInt, name)
  }

}
