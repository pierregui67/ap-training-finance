package com.qfs.sandbox.forecasting;

import java.util.ArrayList;
import java.util.function.Function;

public class GradientDescent {

    private final Double STEP = 0.05;
    private final int ITERATION = 4000;
    private final Double EPSILON = 0.001;

    protected Double[] normalize(Double[] vector) {
        Double norme = 0.0;
        int len = vector.length;
        for (int n = 0; n < len ; n ++) {
            norme += vector[n] * vector[n];
        }
        norme = Math.sqrt(norme);
        for (int n = 0; n < len ; n ++) {
            vector[n] /= norme;
        }
        return vector;
    }

    protected Double[] computeNormalizeGradient(Function<Double[], Double> functionToOptimize, Double[] point) {
        Double[] res = point.clone();
        int len = point.length;
        for (int n = 0; n < len ; n ++) {
            Double[] h = point.clone();
            if (n == 3)
                h[n] += 1;
            else
                h[n] += STEP;
            res[n] = (functionToOptimize.apply(h) - functionToOptimize.apply(point)) / STEP;
        }
        return normalize(res);
    }

    public Double[] minimization(Function<Double[], Double> functionToOptimize, Double[] point) {
        ArrayList<Double> error = new ArrayList<>();
        for (int it = 1; it <= ITERATION; it++ ) {
            Double[] gradient = computeNormalizeGradient(functionToOptimize, point);
            error.add(functionToOptimize.apply(point));
            int len = gradient.length;
            for (int n = 0; n < len ; n ++) {
                point[n] = point[n] * (1 - gradient[n] / 100);
            }
            if (error.size() > 2) {
                if (error.get(error.size() - 2) - error.get(error.size() - 1) < EPSILON)
                    break;
            }
        }
        return point;
    }
}
