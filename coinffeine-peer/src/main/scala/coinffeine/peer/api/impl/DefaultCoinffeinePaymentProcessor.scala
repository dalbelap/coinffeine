package coinffeine.peer.api.impl

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorRef
import akka.pattern._
import com.typesafe.scalalogging.LazyLogging

import coinffeine.model.currency.{Euro, FiatAmount}
import coinffeine.model.payment.PaymentProcessor.AccountId
import coinffeine.peer.api.CoinffeinePaymentProcessor
import coinffeine.peer.payment.PaymentProcessorActor._
import coinffeine.peer.payment.PaymentProcessorProperties

private[impl] class DefaultCoinffeinePaymentProcessor(
    lookupAccountId: () => Option[AccountId],
    override val peer: ActorRef,
    properties: PaymentProcessorProperties)
  extends CoinffeinePaymentProcessor with PeerActorWrapper with LazyLogging {

  override val accountId: Option[AccountId] = lookupAccountId()

  override val balances = properties.balances

  override def currentBalance(): Option[CoinffeinePaymentProcessor.Balance] =
    await((peer ? RetrieveBalance(Euro)).mapTo[RetrieveBalanceResponse].map {
      case BalanceRetrieved(
        balance @ FiatAmount(_, Euro),
        blockedFunds @ FiatAmount(_, Euro)) =>
        Some(CoinffeinePaymentProcessor.Balance(
          balance.asInstanceOf[Euro.Amount],
          blockedFunds.asInstanceOf[Euro.Amount]
        ))
      case nonEurBalance @ BalanceRetrieved(_, _) =>
        logger.error("Balance not in euro: {}", nonEurBalance)
        None
      case BalanceRetrievalFailed(_, cause) =>
        logger.error("Cannot retrieve current balance", cause)
        None
    })
}
