package coinffeine.model.currency

import coinffeine.common.test.UnitTest

class FiatAmountsTest extends UnitTest {

  "Fiat amounts" should "fail to create amounts from duplicated currencies" in {
    an [IllegalArgumentException] shouldBe thrownBy { FiatAmounts.fromAmounts(10.EUR, 5.EUR) }
  }

  it should "create amounts from non duplicated currencies" in {
    val amounts = FiatAmounts.fromAmounts(10.EUR, 5.USD)
    amounts.get(Euro).get shouldBe 10.EUR
    amounts.get(UsDollar).get shouldBe 5.USD
  }

  it should "have no amount for currencies not set" in {
    val amounts = FiatAmounts.fromAmounts(10.EUR)
    amounts.get(UsDollar) shouldBe 'empty
  }

  it should "build by additions to existing amounts" in {
    val amounts = FiatAmounts.empty
      .withAmount(10.EUR)
      .withAmount(5.USD)
    amounts.get(Euro).get shouldBe 10.EUR
    amounts.get(UsDollar).get shouldBe 5.USD
  }

  it should "have an unchecked accessor to amounts" in {
    val amounts = FiatAmounts.fromAmounts(1.EUR)
    noException shouldBe thrownBy { amounts(Euro) }
    a [NoSuchElementException] shouldBe thrownBy { amounts(UsDollar) }
  }

  it should "increment the amount for a given currency" in {
    val amounts = FiatAmounts.fromAmounts(1.EUR).increment(1.EUR)
    amounts(Euro) shouldBe 2.EUR
  }

  it should "create a new amount when incrementing a non-existing one" in {
    val amounts = FiatAmounts.empty.increment(1.EUR)
    amounts(Euro) shouldBe 1.EUR
  }

  it should "decrement the amount for a given currency" in {
    val amounts = FiatAmounts.fromAmounts(1.EUR).decrement(1.EUR)
    amounts(Euro) shouldBe 0.EUR
  }

  it should "not decrement below 0" in {
    val amounts = FiatAmounts.fromAmounts(1.EUR).decrement(10.EUR)
    amounts(Euro) shouldBe 0.EUR
  }
}
