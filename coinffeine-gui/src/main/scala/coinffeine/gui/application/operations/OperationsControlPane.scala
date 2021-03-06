package coinffeine.gui.application.operations

import scala.concurrent.duration._
import scala.util.Try
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._

import coinffeine.gui.application.operations.validation.DefaultOrderValidation
import coinffeine.gui.application.operations.wizard.OrderSubmissionWizard
import coinffeine.gui.beans.Implicits._
import coinffeine.gui.beans.PollingBean
import coinffeine.gui.scene.styles.{ButtonStyles, PaneStyles, TextStyles}
import coinffeine.gui.util.FxExecutor
import coinffeine.model.currency._
import coinffeine.model.market._
import coinffeine.model.order.{OrderRequest, Price}
import coinffeine.peer.api.CoinffeineApp
import coinffeine.protocol.messages.brokerage.Quote

private class OperationsControlPane(app: CoinffeineApp) extends VBox with PaneStyles.Centered {
  id = "operations-control-pane"

  private val validation = new DefaultOrderValidation(app)

  private val bitcoinPrice = new HBox() {
    styleClass += "btc-price"

    private val currentPrice = PollingBean(OperationsControlPane.BitcoinPricePollingInterval) {
      implicit val executor = FxExecutor.asContext
      app.marketStats.currentQuote(Market(Euro)).map(quote => OperationsControlPane.summarize(quote))
    }

    val prelude = new Label("1 BTC = ")

    val amount = new Label with TextStyles.CurrencyAmount {
      text <== currentPrice.mapToString {
        case Some(Some(p)) => p.of(1.BTC).format(Currency.NoSymbol)
        case _ => Euro.formatMissingAmount(Currency.NoSymbol)
      }
    }
    val symbol = new Label(Euro.toString) with TextStyles.CurrencySymbol

    children = Seq(prelude, amount, symbol)
  }

  private val newOrderButton = new Button("New order") with ButtonStyles.Action {
    onAction = submitNewOrder _
  }

  private def submitNewOrder(): Unit = {
    val wizard = new OrderSubmissionWizard(
      app.marketStats, app.utils.exchangeAmountsCalculator, validation)
    Try(wizard.run(Option(delegate.getScene.getWindow))).foreach { data =>
      val request = OrderRequest(data.orderType.value, data.bitcoinAmount.value, data.price.value)
      app.operations.submitOrder(request)
    }
  }

  children = Seq(bitcoinPrice, newOrderButton)
}

object OperationsControlPane {
  private val BitcoinPricePollingInterval = 10.seconds

  /** Summarizes a full quote into a simple price or None if there is no price information at all */
  def summarize(quote: Quote): Option[Price] =
    quote.lastPrice orElse summarize(quote.spread)

  private def summarize(spread: Spread): Option[Price] = {
    val spreadAverage = for {
      bid <- spread.highestBid
      ask <- spread.lowestAsk
    } yield bid.averageWith(ask)

    spreadAverage orElse spread.highestBid orElse spread.lowestAsk
  }
}
