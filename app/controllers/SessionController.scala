package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.openid.{UserInfo, OpenID}
import play.api.libs.concurrent.Execution.Implicits._
import models.account.{TwitterAccount, Account, OpenIdAccount, OpenIdProvider}
import jp.t2v.lab.play2.auth.LoginLogout
import scalikejdbc.DBSession
import play.api.libs.oauth.{RequestToken, OAuth, ServiceInfo, ConsumerKey}
import play.api.Play
import play.api.libs.Crypto
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.Future

trait SessionController extends Controller with LoginLogout with AuthConfigImpl with TxElement {

  def index = Action {
    Ok(views.html.login())
  }

  // =============================================================
  // Twitter
  // =============================================================

  private val Twitter = OAuth(
    ServiceInfo(
      "https://api.twitter.com/oauth/request_token",
      "https://api.twitter.com/oauth/access_token",
      "https://api.twitter.com/oauth/authorize",
      ConsumerKey(
        Play.current.configuration.getString("twitter.consumerKey").get,
        Play.current.configuration.getString("twitter.consumerSecret").get
      )
    ),
    use10a = true
  )

  def loginByTwitter = Action { implicit req =>
    (for {
      t <- Twitter.retrieveRequestToken(routes.SessionController.callbackFromTwitter.absoluteURL()).left.map(e => Redirect(routes.SessionController.index)).right
    } yield {
      Redirect(Twitter.redirectUrl(t.token)).withSession("twitter.requestTokenSecret" -> Crypto.encryptAES(t.secret))
    }).merge
  }

  private val OAuthForm = Form(tuple(
    "oauth_token" -> optional(nonEmptyText),
    "oauth_verifier" -> optional(nonEmptyText),
    "denied" -> optional(nonEmptyText)
  ))

  def callbackFromTwitter = AsyncStack { implicit req =>
    OAuthForm.bindFromRequest.fold({
      _ => Future.successful(Redirect(routes.SessionController.index).flashing("error" -> "ログインに失敗しました。"))
    }, {
      case (Some(oauthToken), Some(oauthVerifier), None) =>
        (for {
          encrypted   <- req.session.get("twitter.requestTokenSecret")
          secret      =  Crypto.decryptAES(encrypted)
          accessToken <- Twitter.retrieveAccessToken(RequestToken(oauthToken, secret), oauthVerifier).right.toOption
        } yield {
          val acc = retrieveUserInfo(accessToken)
          gotoLoginSucceeded(acc.id)
        }) getOrElse Future.successful(Redirect(routes.SessionController.index).flashing("error" -> "ログインに失敗しました。"))
      case _ => Future.successful(Redirect(routes.SessionController.index).flashing("error" -> "ログインに失敗しました。"))
    })
  }

  // 本来なら Service あたりに切り出すべき
  private def retrieveUserInfo(token: RequestToken)(implicit session: DBSession): Account = {
    import twitter4j.TwitterFactory
    import twitter4j.conf._
    val conf = (new ConfigurationBuilder)
      .setOAuthConsumerKey(Twitter.info.key.key)
      .setOAuthConsumerSecret(Twitter.info.key.secret)
      .setOAuthAccessToken(token.token)
      .setOAuthAccessTokenSecret(token.secret)
      .build
    val user = new TwitterFactory(conf).getInstance()
    val existsUser = for {
      tAccount <- TwitterAccount.findById(user.getId)
      account  <- Account.findById(tAccount.accountId)
    } yield account
    existsUser.getOrElse {
      val acc = Account.insert(user.getScreenName)
      TwitterAccount.insert(user.getId, acc.id, user.getScreenName)
      acc
    }
  }


  // =============================================================
  // OpenID
  // =============================================================

  def loginByOpenId(provider: OpenIdProvider) = AsyncStack { implicit req =>
    OpenID.redirectURL(provider.openId, routes.SessionController.callbackFromOpenId(provider).absoluteURL(), Seq("nickname" -> provider.nameAx))
      .map(url => Redirect(url))
      .recover { case t: Throwable => Redirect(routes.SessionController.index) }
  }

  def callbackFromOpenId(provider: OpenIdProvider) = AsyncStack { implicit req =>
    OpenID.verifiedId.flatMap { info =>
      val existsUser = for {
        openid  <- OpenIdAccount.findById(info.id)
        account <- Account.findById(openid.accountId)
      } yield account
      val account = existsUser getOrElse createNewAccount(info, provider)
      gotoLoginSucceeded(account.id)
    }.recover {
      case t: Throwable => Redirect(routes.SessionController.index).flashing("error" -> "ログインに失敗しました。")
    }
  }

  // 本来なら Service あたりに切り出すべき
  // また、Social サービスのアカウントが無くなった場合でも、
  // こちらのアカウントをユーザ側が制御できるように、パスワードだとか
  // メールアドレスとか入力させて作るほうが望ましい
  private def createNewAccount(info: UserInfo, provider: OpenIdProvider)(implicit session: DBSession): Account = {
    val acc = Account.insert(info.attributes.get("nickname").getOrElse(s"unknown@$provider"))
    OpenIdAccount.insert(info.id, provider, acc.id, acc.name)
    acc
  }

}
object SessionController extends SessionController
