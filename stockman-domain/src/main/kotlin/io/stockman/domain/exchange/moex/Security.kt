package io.stockman.domain.exchange.moex

import java.time.LocalDate

/**
 * Objects fields explained:
 * - https://fs.moex.com/files/700
 * - https://doc.stocksharp.ru/html/Properties_T_StockSharp_Micex_Native_Tables_Securities.htm
 *
 * Created by maksim.alekseev on 17/09/2018
 */
data class Security (

        val secId: String,

        val boardId: String,

        /**
         * The short name for the security.
         */
        val shortName: String,

        /**
         * Weighted average price of previous trade session.
         */
        val prevWaPrice: Double,

        /**
         * The interest rate according to the weighted average price of previous trade session.
         */
        val yieldAtPrevWaPrice: Double,

        /**
         * Value of the coupon expressed in currency (used for coupon securities).
         */
        val couponValue: Double,

        /**
         * The date when next coupon will be paid (used for coupon securities).
         */
        val nextCoupon: LocalDate,

        /**
         * The accrued interest for date of tading session in calculation on one security expressed in currency.
         */
        val accruedInt: Double,

        /**
         * Closing price of the previous trading session.
         */
        val prevPrice: Double,

        /**
         * Quantity of the securities in one standard lot.
         */
        val lotSize: Int,

        /**
         * The face value of one security, in currency of the security.
         */
        val faceValue: Double,

        /**
         * Board name.
         */
        val boardName: String,

        /**
         * The indicator "the trade operations permit / prohibit".
         * A  -  Active (operations are permitted)
         * S  -  Suspended (operations are prohibited)
         */
        val status: String,

        /**
         * The maturity date.
         */
        val matDate: LocalDate,

        /**
         * The number of implied decimals in prices for the security.
         */
        val decimals: Int,

        /**
         * Duration of the coupon expressed in days (used for coupon securities).
         */
        val couponPeriod: Int,

        /**
         * Issue size.
         */
        val issueSize: Long,

        /**
         * Official close price of the previous trading session
         */
        val prevLegalClosePrice: Double,

        /**
         * Admitted quote of the previous trading session
         */
        val prevAdmittedQuote: Double,

        /**
         * Date of last trading session.
         */
        val prevDate: LocalDate,

        /**
         * The security name.
         */
        val secName: String,

        /**
         * The security remarks.
         */
        val remarks: String,

        /**
         * The identifier of the market on which one is traded the security.
         */
        val marketCode: String,

        /**
         * The instrument code for the security.
         */
        val instrId: String,

        /**
         * The sector code for the security.
         */
        val sectorId: String,

        /**
         * The minimum price step for buy/sell orders with this security.
         */
        val minStep: Double,

        /**
         * The code of currency, in which one is expressed a rating of the security.
         */
        val faceUnit: String,

        /**
         * The buy back price in REPO operations or buy back operations of issiuers.
         */
        val buybackPrice: Double,

        /**
         * Date of buy back operation.
         */
        val buybackDate: LocalDate,

        /**
         * International security identification number.
         */
        val isin: String,

        /**
         * The security name in English.
         */
        val latName: String,

        /**
         * State registration number.
         */
        val regNumber: String,

        /**
         * The code of linked currency of the security.
         */
        val currencyId: String,

        /**
         * Number of placed (traded) securities in issue.
         */
        val issueSizePlaced: Long,

        /**
         * List level.
         */
        val listLevel: Int,

        /**
         * Security type.
         */
        val secType: String,

        /**
         * Coupon percentage (used for coupon securities).
         */
        val couponPercent: Double,

        /**
         * Date of
         */
        val offerDate: LocalDate,

        /**
         * The date of settlement according to settle mode for the security.
         */
        val settleDate: LocalDate

)
