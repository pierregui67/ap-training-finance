package com.qfs.sandbox.forecasting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGradientDescent {

    final int NUMBER_ITERATION = 50;

    protected final Double f(Double[] param) {
        return (param[0] * param[0]) + (param[0] * param[1] * param[2]) + param[2];

    }

    protected final Double[] fGradient(Double[] param) {
        Double[] trueGradient = new Double[] {2 * param[0] + param[1] * param[2], param[1] * param[2],
                param[0] * param[1] + 1.0};
        return trueGradient;

    }

    protected final Double g(Double[] param) {
        Double x = param[0], y = param[1], z = param[2];
        return x*y*z + Math.pow(x,3) * Math.pow(z,4) + Math.pow(y,3)*Math.pow(z,2)*x + x;

    }

    protected final Double[] gGradient(Double[] param) {
        Double x = param[0], y = param[1], z = param[2];
        Double[] trueGradient = new Double[] {
                y*z + 3*Math.pow(x,2)*Math.pow(z,4) + Math.pow(y,3)*Math.pow(z,2) + 1,
                x*z + 3*Math.pow(y*z,2)*x,
                x*y + 4*Math.pow(x*z,3) + 2*Math.pow(y,3)*x*z
        };
        return trueGradient;

    }

    public Double computeError(int order) {
        GradientDescent gradientDescent = new GradientDescent(order);
        Double error;
        Double sumError = 0.0;
        for (int i = 0; i < NUMBER_ITERATION; i++) {
            Double x = (i - NUMBER_ITERATION / 2.0) / NUMBER_ITERATION;
            for (int j = 0; j < NUMBER_ITERATION; j++) {
                Double y = (j - NUMBER_ITERATION / 2.0) / NUMBER_ITERATION;
                for (int k = 0; k < NUMBER_ITERATION; k++) {
                    Double z = (k - NUMBER_ITERATION / 2.0) / NUMBER_ITERATION;
                    Double[] computedGrad = gradientDescent.computeGradient(this::g, new Double[]{x, y, z});
                    Double[] trueGrad = gGradient(new Double[]{x, y, z});
                    error = Math.pow((computedGrad[0] - trueGrad[0]), 2)
                            + Math.pow((computedGrad[1] - trueGrad[1]), 2)
                            + Math.pow((computedGrad[2] - trueGrad[2]), 2);
                    sumError += error;
                }
            }
        }
        return sumError;
    }

    @Test
    public void testComputeGradient() throws Exception{
        //Double error6 = computeError(6);
        Double error2 = computeError(2);
        Double error4 = computeError(4);
        boolean b = (error2 < error4); //&& (error4 < error6);
        assertTrue(b);
    }
}
