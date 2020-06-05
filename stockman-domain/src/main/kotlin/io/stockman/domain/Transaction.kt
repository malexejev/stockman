package io.stockman.domain

import java.time.LocalDate
import javax.money.MonetaryAmount

/**
 * Transaction is a date-stamped monetary amount.
 *
 * Created by maksim.alekseev on 03/05/2018
 */
data class Transaction (
    val amount: MonetaryAmount,
    val date: LocalDate
)