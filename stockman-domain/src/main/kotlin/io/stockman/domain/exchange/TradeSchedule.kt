package io.stockman.domain.exchange

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.of

/**
 * Trading calendar of particular exchange or exchange market.
 *
 * In reality, its more complex:
 * - schedule is set per market section
 * - separate schedules exist for actual trading and clearing
 * - its not just open/close hours, but also gaps for clearing for example
 *
 * But first implementation will be as simple as possible. Some refs for future improvements:
 * - https://fs.moex.com/f/10150/2018-09-24-torgovyy-kalendar-na-2019-god.pdf
 * - https://www.moex.com/s1167
 * - https://mrtopstep.com/nyse-holidays-market-closings-2017-stock-market-holidays-schedule/
 *
 * Created by maksim.alekseev on 2019-02-17
 */
interface TradeSchedule {

    fun isOpen(day: LocalDate): Boolean

    fun opensAt(day: LocalDate): LocalDateTime?

    fun closesAt(day: LocalDate): LocalDateTime?

}

/**
 * Simplistic schedule that removes weekends.
 */
object SimpleRuSchedule: TradeSchedule {

    override fun isOpen(day: LocalDate): Boolean {
        return day.dayOfWeek.value <= 5;
    }

    override fun opensAt(day: LocalDate): LocalDateTime? {
        return if (isOpen(day)) of(day.year, day.month, day.dayOfMonth, 9, 30, 0) else null;
    }

    override fun closesAt(day: LocalDate): LocalDateTime? {
        return if (isOpen(day)) of(day.year, day.month, day.dayOfMonth, 19, 0, 0) else null;
    }

}
