package io.stockman.domain

import io.stockman.domain.exchange.Exchange

/**
 * TradingContext defines environment where trade happens.
 * It is a set of attributes common for a number of trades. Usually trades are grouped by exchange,
 * market or market section, broker, and broker account.
 *
 * This context is passed to all services responsible for trades processing, so that they can use any attributes
 * of the trade environment to make their computations.
 *
 * Created by maksim.alekseev on 2019-03-10
 */
class TradingContext {

//    private var exchange: Exchange;
//    private var broker: Broker;

}