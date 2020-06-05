package io.stockman;

import org.junit.Test;

import java.time.LocalDate;

/**
 * Temporary test to manually compute YTM of selected bonds
 *
 * Created by maksim.alekseev on 28/06/2018
 */
public class BondYtmTest {

    private static final double brokerComission = 0d;
    private static final double exchangeComission = 0d;

    @Test
    public void computeForOne() {

    }

    protected double getYtm(
            double price,
            double coupon,
            int couponsPerYear,
            LocalDate nextCouponDate,
            LocalDate maturityDate) {
        return 0d;
    }

}
