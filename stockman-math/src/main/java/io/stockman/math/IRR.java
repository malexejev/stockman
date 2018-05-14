package io.stockman.math;

import io.stockman.domain.Transaction;
import io.stockman.math.algo.NewtonRaphsonSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.pow;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * See
 * 1. https://www.investopedia.com/terms/i/irr.asp
 * 2. http://www.utstat.utoronto.ca/~alexander/irr-soln.pdf
 *
 * Created by maksim.alekseev on 20/04/2018
 */
public class IRR {

    private static final Logger LOG = LoggerFactory.getLogger(IRR.class);

    private final double guess;
    private final double tolerance;
    private final int itersLimit;

    public IRR(double guess, double tolerance, int itersLimit) {
        this.guess = guess <= -1d ? 0d : guess;
        this.tolerance = tolerance;
        this.itersLimit = itersLimit;
    }

    /**
     * @param transactions cashflow
     * @return IRR, i.e. discount rate when NPV = 0
     */
    public OptionalDouble compute(List<Transaction> transactions) {
        if (transactions.size() < 2) {
            throw new IllegalArgumentException(
                    "IRR requires casflow of at least 2 transactions. Provided cashflow " + transactions);
        }

        long[] dates = new long[transactions.size()];
        double[] amounts = new double[transactions.size()];

        LocalDate start = transactions.get(0).getDate();
        for (int i = 0; i < transactions.size(); i++) {
            // dates are relative here, and dates[0] == 0
            dates[i] = DAYS.between(start, transactions.get(i).getDate());
            amounts[i] = transactions.get(i).getAmount().getNumber().doubleValueExact();
        }

        DoubleUnaryOperator npv = x -> {
            double f = amounts[0];
            for (int i = 1; i < dates.length; i++) {
                f += amounts[i] / pow(1d + x, dates[i] / 365d);
            }
            return f;
        };

        // http://www.wolframalpha.com/input/?i=d/dx a/(1+x)^(n/365)
        DoubleUnaryOperator d_npv = x -> {
            double df = 0d;
            for (int i = 1; i < dates.length; i++) {
                df -= dates[i] * amounts[i] / 365d * pow(1d + x, -(1d + dates[i] / 365d));
            }
            return df;
        };

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver(npv, d_npv, guess, tolerance, itersLimit);
        try {
            double root = solver.solve();
            return root > -1d ? OptionalDouble.of(root) : OptionalDouble.empty();
        } catch (IllegalStateException e) {
            LOG.error("IRR equation root not found for cash flow {}", transactions, e);
            return OptionalDouble.empty();
        }
    }

}
