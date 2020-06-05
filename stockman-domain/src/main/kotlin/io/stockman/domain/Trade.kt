package io.stockman.domain

import io.stockman.domain.exchange.moex.Security
import javax.money.MonetaryAmount

/**
 * Single trade: buying or selling particular security for particular price.
 *
 * Created by maksim.alekseev on 2019-03-10
 */
data class Trade (
        val type: TradeType,
        val security: Security,
        val price: MonetaryAmount,
        val quantity: Int
)