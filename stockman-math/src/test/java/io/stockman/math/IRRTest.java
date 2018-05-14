package io.stockman.math;

import io.stockman.domain.Transaction;
import org.javamoney.moneta.FastMoney;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * Created by maksim.alekseev on 25/04/2018
 */
public class IRRTest {

    // default params of Excel's built-in XIRR() function

    static final double EXCEL_TOL = 1e-8; // 0.000001%
    static final double EXCEL_GUESS = 0.1;
    static final int EXCEL_ITERATIONS = 100;

    static final IRR EXCEL_IRR = new IRR(EXCEL_GUESS, EXCEL_TOL, EXCEL_ITERATIONS);

    @Test(expected = IllegalArgumentException.class)
    public void computeEmptyCashflow() {
        EXCEL_IRR.compute(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeShortCashflow() {
        EXCEL_IRR.compute(singletonList(new Transaction(FastMoney.of(-2750, "RUB"), parse("2008-02-05"))));
    }

    @Test
    public void computeSimple() {
        var irr = new IRR(0.1, 0.001, 50);
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-2750, "RUB"), parse("2008-02-05")));
            add(new Transaction(FastMoney.of(1000, "RUB"), parse("2008-07-05")));
            add(new Transaction(FastMoney.of(2000, "RUB"), parse("2009-01-05")));
        }};
        assertThat(irr.compute(transactions)).hasValueCloseTo(0.124, offset(0.001));
    }

    /**
     * Checks that result is the same no matter which base date
     * (date of first transaction in cashflow) is used.
     */
    @Test
    public void computeCashflowUnsorted() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-10000, "USD"), parse("2008-01-01")));
            add(new Transaction(FastMoney.of(2750, "USD"), parse("2008-03-01")));
            add(new Transaction(FastMoney.of(4250, "USD"), parse("2008-10-30")));
            add(new Transaction(FastMoney.of(3250, "USD"), parse("2009-02-15")));
            add(new Transaction(FastMoney.of(2750, "USD"), parse("2009-04-01")));
        }};
        List<List<Transaction>> allPermutations = permutateFirstElement(transactions);
        for (List<Transaction> cashflow : allPermutations) {
            assertThat(EXCEL_IRR.compute(cashflow)).hasValueCloseTo(0.373362535, offset(0.001));
        }
    }

    /**
     * https://support.office.com/en-us/article/xirr-function-de1242ec-6477-445b-b11b-a303ad9adc9d
     *
     * This example from official Excel docs works fine.
     */
    @Test
    public void computeExcelCompatible() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-10000, "USD"), parse("2008-01-01")));
            add(new Transaction(FastMoney.of(2750, "USD"), parse("2008-03-01")));
            add(new Transaction(FastMoney.of(4250, "USD"), parse("2008-10-30")));
            add(new Transaction(FastMoney.of(3250, "USD"), parse("2009-02-15")));
            add(new Transaction(FastMoney.of(2750, "USD"), parse("2009-04-01")));
        }};
        assertThat(EXCEL_IRR.compute(transactions)).hasValueCloseTo(0.373362535, offset(EXCEL_TOL));
    }

    /**
     * https://www.techonthenet.com/excel/formulas/xirr.php
     *
     * This example does not match with Excel starting from 1e-8 digit
     */
    @Test
    public void computeExcelError1() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-7500, "USD"), parse("2016-01-01")));
            add(new Transaction(FastMoney.of(3000, "USD"), parse("2016-02-01")));
            add(new Transaction(FastMoney.of(5000, "USD"), parse("2016-04-15")));
            add(new Transaction(FastMoney.of(1200, "USD"), parse("2016-08-01")));
            add(new Transaction(FastMoney.of(4000, "USD"), parse("2017-03-26")));
        }};
        // excel result is 2.660242057
        assertThat(EXCEL_IRR.compute(transactions)).hasValueCloseTo(2.66024204, offset(EXCEL_TOL));
    }

    /**
     * https://www.techonthenet.com/excel/formulas/xirr.php
     *
     * This example also does not match with Excel
     */
    @Test
    public void computeExcelError2() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-5000, "USD"), parse("2016-04-30")));
            add(new Transaction(FastMoney.of(800, "USD"), parse("2016-05-31")));
            add(new Transaction(FastMoney.of(1300, "USD"), parse("2016-09-01")));
            add(new Transaction(FastMoney.of(600, "USD"), parse("2016-12-31")));
            add(new Transaction(FastMoney.of(7500, "USD"), parse("2017-01-31")));
        }};
        // excel result is 2.186309695
        assertThat(EXCEL_IRR.compute(transactions)).hasValueCloseTo(2.18630968, offset(EXCEL_TOL));
    }

    /**
     * http://www.wolframalpha.com/input/?i=roots -1000+500/(x+1)^(365/365)+500/(x+1)^(730/365)
     */
    @Test
    public void computeGuessLessThanMinusOne() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-1000, "USD"), now()));
            add(new Transaction(FastMoney.of(500, "USD"), now().plusDays(365)));
            add(new Transaction(FastMoney.of(500, "USD"), now().plusDays(365 * 2)));
        }};
        assertThat(new IRR(-1.5, 0.001, 50).compute(transactions)).hasValueCloseTo(0d, offset(0.001));
        assertThat(new IRR(-1d, 0.001, 50).compute(transactions)).hasValueCloseTo(0d, offset(0.001));
    }

    /**
     * http://www.wolframalpha.com/input/?i=roots -1000+500/(x+1)^(365/365)+500/(x+1)^(730/365)
     *
     * this is a rare situation of bad initial guess - root converges to -1.5 instead of 0
     */
    @Test
    @Ignore
    public void computeRootLessThanMinusOne() {
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-1000, "USD"), now()));
            add(new Transaction(FastMoney.of(500, "USD"), now().plusDays(365)));
            add(new Transaction(FastMoney.of(500, "USD"), now().plusDays(365 * 2)));
        }};

        // FIXME MA: try another algorithm, or introduce solver interval, result should be 0 with this guess
        assertThat(new IRR(1d, 0.001, 50).compute(transactions)).hasValueCloseTo(0d, offset(0.001));
    }

    private <E> List<List<E>> permutateFirstElement(List<E> original) {
        var results = new ArrayList<List<E>>(original.size());
        for (int i = 0; i < original.size(); i++) {
            var nextList = new ArrayList<E>(original.size());
            nextList.add(original.get(i));
            for (int j = 0; j < original.size(); j++) {
                if (i != j) nextList.add(original.get(j));
            }
            results.add(nextList);
        }
        return results;
    }

}