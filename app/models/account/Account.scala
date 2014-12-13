package models.account

import scalikejdbc._

case class Account(id: AccountId, name: String, twitter: Option[TwitterAccount] = None, yahoo: Option[OpenIdAccount] = None)

object Account extends SQLSyntaxSupport[Account] {

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = autoConstruct(rs, s, "twitter", "yahoo")

  private val a = Account.syntax("a")
  private val t = TwitterAccount.syntax("t")
  private val y = OpenIdAccount.syntax("y")
  private val ac = Account.column

  def findById(id: AccountId)(implicit session: DBSession): Option[Account] = {
    withSQL {
      select
        .from(Account as a)
        .leftJoin(TwitterAccount as t).on(a.id, t.accountId)
        .leftJoin(OpenIdAccount as y).on(a.id, y.accountId)
      .where.eq(a.id, id)
    }.map { rs =>
      Account(a)(rs).copy(
        twitter = rs.longOpt(t.resultName.id).map(_ => TwitterAccount(t)(rs)),
        yahoo   = rs.stringOpt(y.resultName.id).map(_ => OpenIdAccount(y)(rs))
      )
    }.first().apply()
  }

  def insert(name: String)(implicit session: DBSession): Account = {
    val id = applyUpdateAndReturnGeneratedKey {
      QueryDSL.insert.into(Account).namedValues(ac.name -> name)
    }
    Account(id.toInt, name)
  }

}
