package coinffeine.peer.events.fiat

import coinffeine.common.akka.event.TopicProvider
import coinffeine.model.currency.FiatAmounts
import coinffeine.model.util.Cached

/** An event published when FIAT balance changes. */
case class BalanceChanged(balance: Cached[FiatAmounts])

object BalanceChanged extends TopicProvider[BalanceChanged] {
  override val Topic = "fiat.balance-changed"
}
