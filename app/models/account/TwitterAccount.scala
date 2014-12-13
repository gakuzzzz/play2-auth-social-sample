package models.account

import scalikejdbc._

case class TwitterAccount(id: TwitterAccountId, accountId: AccountId, screenName: String)
object TwitterAccount extends SQLSyntaxSupport[TwitterAccount] {

  def apply(s: SyntaxProvider[TwitterAccount])(rs: WrappedResultSet): TwitterAccount = autoConstruct(rs, s)

  private val t = TwitterAccount.syntax("t")
  private val tc = TwitterAccount.column

  def findById(id: TwitterAccountId)(implicit session: DBSession): Option[TwitterAccount] = {
    withSQL {
      select
        .from(TwitterAccount as t)
        .where.eq(t.id, id)
    }.map(TwitterAccount(t)).first().apply()
  }

  def insert(id: TwitterAccountId, accountId: AccountId, screenName: String)(implicit session: DBSession): TwitterAccount = {
    applyUpdate {
      QueryDSL.insert.into(TwitterAccount).namedValues(
        tc.id         -> id,
        tc.accountId  -> accountId,
        tc.screenName -> screenName
      )
    }
    TwitterAccount(id, accountId, screenName)
  }

}
