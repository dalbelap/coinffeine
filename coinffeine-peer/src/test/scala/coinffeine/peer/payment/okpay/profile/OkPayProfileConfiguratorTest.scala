package coinffeine.peer.payment.okpay.profile

import coinffeine.common.test.{FutureMatchers, UnitTest}
import coinffeine.peer.payment.okpay.OkPayApiCredentials

class OkPayProfileConfiguratorTest extends UnitTest with FutureMatchers {

  "An OKPay profile configurator" should "switch to business mode from other modes" in
    new Fixture {
      configurator.configure().futureValue
      profile.accountMode shouldBe AccountMode.Business
    }

  it should "enable API use" in new Fixture {
    configurator.configure().futureValue
    profile.apiEnabled shouldBe true
  }

  it should "configure and return API credentials" in new Fixture {
    val credentials = configurator.configure().futureValue
    profile.seedToken should not be 'empty
    credentials shouldBe OkPayApiCredentials(profile.walletId, profile.seedToken.get)
  }

  trait Fixture {
    protected val profile = new FakeProfile()
    protected val configurator = new OkPayProfileConfigurator(profile)
  }

  class FakeProfile extends Profile {
    override val walletId = "FakeId001"
    override var accountMode: AccountMode = AccountMode.Client
    var apiEnabled = false
    var seedToken: Option[String] = None

    override def enableAPI(id: String): Unit = {
      require(id == walletId)
      apiEnabled = true
    }

    override def configureSeedToken(id: String) = {
      require(id == walletId)
      seedToken = Some("seedToken")
      seedToken.get
    }
  }
}
