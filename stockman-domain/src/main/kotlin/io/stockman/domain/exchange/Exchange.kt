package io.stockman.domain.exchange;

import java.time.LocalDate
import java.time.ZoneId

/**
 * Represents some financial exchange, such as stock exchange or foreign exchange.
 *
 * Created by maksim.alekseev on 2019-02-17
 */
enum class Exchange(

        val shortName: String,
        val fullName: String

//        val commission: Double,
//
//        val timeZone: ZoneId,
//        val schedule: TradeSchedule
//
) {

    MOEX("MOEX", "Moscow Exchange"),
    SPBEX("SPB", "Saint-Petersburg Exchange"),
    NYSE("NYSE", "New York Stock Exchange")
}
