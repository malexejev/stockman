package io.stockman.math;

import io.stockman.domain.Transaction;
import io.stockman.math.algo.NewtonRaphsonSolver;
import org.javamoney.moneta.FastMoney;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static io.stockman.math.IRRTest.EXCEL_GUESS;
import static io.stockman.math.IRRTest.EXCEL_ITERATIONS;
import static io.stockman.math.IRRTest.EXCEL_TOL;
import static java.lang.Math.pow;
import static java.math.BigDecimal.valueOf;
import static java.time.LocalDate.parse;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * These tests are here to prove computation precision of double vs BigDecimal,
 * Money vs FastMoney and other critical data type choices.
 *
 * Results:
 * - primitive 'double' precision is enough for our computations
 * - looks like excel XIRR does not satisfy error tolerance stated in documentation
 *
 * Created by maksim.alekseev on 10/05/2018
 */
public class HighPrecisionIRRTest {

    /**
     * tolerance        0.000001 percent
     *
     * Excel		    2.660242057
     * double		    2.6602420360887016
     * BigDec Money	    2.6602420360887020668930425783607099810063003430022
     * BigDec FastMoney	2.6602420360887020668930425783607099810063003430022
     *
     * Excel  NPV =    -0.00001452764981546791971814195103401497014638260
     * double NPV =     0.00000000000011941677519945054944121656712450972
     * BigDec NPV =    -0.00000000000004373197880989120543216957497590835
     */
    @Test
    @Ignore
    public void testAccuracy() {
        var irr = new HighPrecisionIRR(EXCEL_GUESS, EXCEL_TOL, EXCEL_ITERATIONS);
        var transactions = new ArrayList<Transaction>(){{
            add(new Transaction(FastMoney.of(-7500, "USD"), parse("2016-01-01")));
            add(new Transaction(FastMoney.of(3000, "USD"), parse("2016-02-01")));
            add(new Transaction(FastMoney.of(5000, "USD"), parse("2016-04-15")));
            add(new Transaction(FastMoney.of(1200, "USD"), parse("2016-08-01")));
            add(new Transaction(FastMoney.of(4000, "USD"), parse("2017-03-26")));
        }};

        BigDecimal root = irr.compute(transactions).get();

        var npv = irr.getNpvFunc(transactions);
        System.out.println("Excel  NPV = " + npv.apply(valueOf(2.6602420568466200)));
        System.out.println("double NPV = " + npv.apply(valueOf(2.6602420360887016)).toPlainString());
        System.out.println("bigdec NPV = " + npv.apply(root).toPlainString());

        // this assert fail because Excel value (expected) does not satisfy tolerance
        assertThat(root).isCloseTo(valueOf(2.660242057), offset(valueOf(EXCEL_TOL)));
    }

}

/**
 * Version of {@link IRR} based on {@link BigDecimal}.
 * Mostly for testing and validation purpose.
 *
 * Created by maksim.alekseev on 24/04/2018
 */
class HighPrecisionIRR {

    private static final Logger LOG = LoggerFactory.getLogger(HighPrecisionIRR.class);

    private static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);

    private final BigDecimal guess;
    private final BigDecimal tolerance;
    private final int itersLimit;

    public HighPrecisionIRR(double guess, double tolerance, int itersLimit) {
        this.itersLimit = itersLimit;
        this.tolerance = valueOf(tolerance);
        this.guess = valueOf(guess);
    }

    public Optional<BigDecimal> compute(List<Transaction> transactions) {
        long[] dates = new long[transactions.size()];
        BigDecimal[] amounts = new BigDecimal[transactions.size()];

        LocalDate start = transactions.get(0).getDate();
        for (int i = 0; i < transactions.size(); i++) {
            // dates are relative here, and dates[0] == 0
            dates[i] = DAYS.between(start, transactions.get(i).getDate());
            amounts[i] = valueOf(transactions.get(i).getAmount().getNumber().doubleValueExact());
        }

        UnaryOperator<BigDecimal> npv = x -> {
            BigDecimal f = amounts[0];
            for (int i = 1; i < dates.length; i++) {
                f = f.add(amounts[i].divide(valueOf(pow(1d + x.doubleValue(), dates[i] / 365d)), MC), MC);
            }
            return f;
        };

        // check another function from excel improvement article
        UnaryOperator<BigDecimal> d_npv = x -> {
            BigDecimal df = BigDecimal.ZERO;
            for (int i = 1; i < dates.length; i++) {
                df = df.subtract(valueOf(dates[i]).multiply(amounts[i], MC).divide(valueOf(365d), MC).multiply(valueOf(pow(1d + x.doubleValue(), -1d - dates[i] / 365d)), MC), MC);
            }
            return df;
        };

        var solver = new HighPrecisionNewtonSolver(npv, d_npv, guess, tolerance, itersLimit, MC);
        try {
            BigDecimal root = solver.solve();
            return Optional.of(root).filter(r -> r.compareTo(valueOf(-1L)) > 0);
        } catch (IllegalStateException e) {
            LOG.error("IRR equation root not found for cash flow {}", transactions, e);
            return Optional.empty();
        }
    }

    public UnaryOperator<BigDecimal> getNpvFunc(List<Transaction> transactions) {
        long[] dates = new long[transactions.size()];
        BigDecimal[] amounts = new BigDecimal[transactions.size()];

        LocalDate start = transactions.get(0).getDate();
        for (int i = 0; i < transactions.size(); i++) {
            // dates are relative here, and dates[0] == 0
            dates[i] = DAYS.between(start, transactions.get(i).getDate());
            amounts[i] = valueOf(transactions.get(i).getAmount().getNumber().doubleValueExact());
        }

        UnaryOperator<BigDecimal> npv = x -> {
            BigDecimal f = amounts[0];
            for (int i = 1; i < dates.length; i++) {
                f = f.add(amounts[i].divide(valueOf(pow(1d + x.doubleValue(), dates[i] / 365d)), MC), MC);
            }
            return f;
        };
        return npv;
    }

}

/**
 * Version of {@link NewtonRaphsonSolver} based on {@link BigDecimal}.
 * Mostly for testing and validation purpose.
 *
 * Created by maksim.alekseev on 24/04/2018
 */
class HighPrecisionNewtonSolver {

    private static final String DIV_BY_0_TEMPLATE = "Derivative is zero at x = %s, iteration %s. Try another guess.";
    private static final String ITER_LIMIT_TEMPLATE = "Exceeded max iterations limit of %s.";

    private final BigDecimal guess;
    private final BigDecimal tolerance;
    private final int iterationsLimit;
    private final MathContext mc;

    // function to solve
    private final UnaryOperator<BigDecimal> f;

    // derivative of the function to solve
    private final UnaryOperator<BigDecimal> df;

    public HighPrecisionNewtonSolver(UnaryOperator<BigDecimal> f,
                                     UnaryOperator<BigDecimal> df,
                                     BigDecimal guess,
                                     BigDecimal tolerance,
                                     int iterationsLimit,
                                     MathContext mc) {
        this.f = f;
        this.df = df;
        this.mc = mc;
        this.guess = guess;
        this.tolerance = tolerance;
        this.iterationsLimit = iterationsLimit;
    }

    public BigDecimal solve() {
        BigDecimal x0 = guess, x1, err = valueOf(Double.MAX_VALUE);
        int iter = 0;
        while (err.compareTo(tolerance) > 0 && iter++ < iterationsLimit) {
            BigDecimal fx = f.apply(x0);
            BigDecimal dfx = df.apply(x0);
            if (dfx.equals(BigDecimal.ZERO)) {
                throw new IllegalStateException(String.format(DIV_BY_0_TEMPLATE, x0, iter));
            }
            x1 = x0.subtract(fx.divide(dfx, mc), mc);
            err = x1.compareTo(x0) > 0 ? x1.subtract(x0, mc) : x0.subtract(x1, mc);
            x0 = x1;
        }
        if (iter <= iterationsLimit) {
            return x0;
        } else {
            throw new IllegalStateException(String.format(ITER_LIMIT_TEMPLATE, iterationsLimit));
        }
    }

}
