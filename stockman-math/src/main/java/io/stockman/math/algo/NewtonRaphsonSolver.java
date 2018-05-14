package io.stockman.math.algo;

import java.util.function.DoubleUnaryOperator;

/**
 * Root search algorithm implementation with Newton-Raphson Method
 *
 * See
 * 1. https://en.wikipedia.org/wiki/Newton%27s_method
 * 2. https://www.math.ubc.ca/~anstee/math104/104newtonmethod.pdf
 * 3. https://excel.uservoice.com/forums/304921/suggestions/18590770-fix-irr-xirr
 *
 * Created by maksim.alekseev on 24/04/2018
 */
public class NewtonRaphsonSolver {

    private static final String DIV_BY_0_TEMPLATE = "Derivative is zero at x = %s, iteration %s. Try another guess.";
    private static final String ITER_LIMIT_TEMPLATE = "Exceeded max iterations limit of %s.";

    private final double guess;
    private final double tolerance;
    private final int iterationsLimit;

    // function to solve
    private final DoubleUnaryOperator f;

    // derivative of the function to solve
    private final DoubleUnaryOperator df;

    public NewtonRaphsonSolver(DoubleUnaryOperator f,
                               DoubleUnaryOperator df,
                               double guess,
                               double tolerance,
                               int iterationsLimit) {
        this.f = f;
        this.df = df;
        this.guess = guess;
        this.tolerance = tolerance;
        this.iterationsLimit = iterationsLimit;
    }

    public double solve() {
        double x1, x0 = guess, err = Double.MAX_VALUE;
        int iter = 0;
        while (err > tolerance && iter++ < iterationsLimit) {
            double fx = f.applyAsDouble(x0);
            double dfx = df.applyAsDouble(x0);
            if (dfx == 0) {
                throw new IllegalStateException(String.format(DIV_BY_0_TEMPLATE, x0, iter));
            }
            x1 = x0 - fx / dfx;
            err = Math.abs(x1 - x0);
            x0 = x1;
        }
        if (iter <= iterationsLimit) {
            return x0;
        } else {
            throw new IllegalStateException(String.format(ITER_LIMIT_TEMPLATE, iterationsLimit));
        }
    }

}
