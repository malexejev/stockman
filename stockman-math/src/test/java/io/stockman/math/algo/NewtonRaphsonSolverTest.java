package io.stockman.math.algo;

import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by maksim.alekseev on 25/04/2018
 */
public class NewtonRaphsonSolverTest {

    private static final double TOL = 0.001d;

    @Test
    public void solveLinear() {
        DoubleUnaryOperator f = x -> x - 2;
        DoubleUnaryOperator df = x -> 1d;
        var solver = new NewtonRaphsonSolver(f, df, 0.5, TOL, 10);
        assertThat(solver.solve()).isCloseTo(2, Offset.offset(TOL));
    }

    @Test
    public void solveQuadratic() {
        DoubleUnaryOperator f = x -> x*x - 3*x + 1;
        DoubleUnaryOperator df = x -> 2*x - 3;

        // close to 1st root
        assertThat(new NewtonRaphsonSolver(f, df, 0.5, TOL, 10).solve())
                .isCloseTo(0.381966, Offset.offset(TOL));

        // close to 2nd root
        assertThat(new NewtonRaphsonSolver(f, df, 2.5, TOL, 10).solve())
                .isCloseTo(2.61803, Offset.offset(TOL));
    }

    @Test(expected = IllegalStateException.class)
    public void solveQuadraticFromExtremum() {
        DoubleUnaryOperator f = x -> x*x - 3*x + 1;
        DoubleUnaryOperator df = x -> 2*x - 3;

        // middle point between two roots and df(1.5) == 0
        assertThat(new NewtonRaphsonSolver(f, df, 1.5, TOL, 10).solve())
                .isCloseTo(0.381966, Offset.offset(TOL));
    }

    @Test(expected = IllegalStateException.class)
    public void solveNoRoots() {
        DoubleUnaryOperator f = x -> x * x - 3 * x + 4;
        DoubleUnaryOperator df = x -> 2*x - 3;

        assertThat(new NewtonRaphsonSolver(f, df, 2, TOL, 10).solve())
                .isCloseTo(0, Offset.offset(TOL));
    }

}