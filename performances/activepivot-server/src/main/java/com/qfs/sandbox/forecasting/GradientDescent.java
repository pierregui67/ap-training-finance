package com.qfs.sandbox.forecasting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class GradientDescent {

    /** Map the accuracy order to its associated coefficients */
    private final HashMap<Integer,Double[]> finiteDifferenceCoefficents = new HashMap<>();

    private final int orderAccuracy;
    private final Double STEP = .001;
    private final int ITERATION = 4000;
    private final Double EPSILON = 0.001;

    public GradientDescent() {
        init();
        orderAccuracy = 2;
    }

    public GradientDescent(int order) {
        init();
        if (finiteDifferenceCoefficents.containsKey(order))
            orderAccuracy = order;
        else {
            System.out.println("Their is no implemented schema for this accuracy order");
            orderAccuracy = 2;
        }
    }

    /** https://en.wikipedia.org/wiki/Finite_difference_coefficient */
    public void init() {
        // 2-order accuracy
        finiteDifferenceCoefficents.put(2, new Double[] {0.0, 0.5});
        // 4-order accuracy
        finiteDifferenceCoefficents.put(4, new Double[] {0.0, 2.0 / 3, - 1.0 / 12});
        // 6-order accuracy
        finiteDifferenceCoefficents.put(6, new Double[] {0.0, 3.0 / 4, - 3.0 / 20, 1.0 / 60});

    }

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

    /**
    Compute the gradient with a eight-order accuracy.
     */
    protected Double[] computeGradient(Function<Double[], Double> functionToOptimize, Double[] point) {
        Double[] res = point.clone();
        int len = point.length;
        for (int n = 0; n < len ; n ++) {
            Double[] coefficients = this.finiteDifferenceCoefficents.get(orderAccuracy);
            int numberOfCoefficients = coefficients.length;
            HashMap<Integer, Double[]> H = new HashMap<>();
            for (int i = 0; i < numberOfCoefficients; i ++) {
                Double[] hForward = point.clone();
                hForward[n] += (STEP * i);
                Double[] hBackward = point.clone();
                hBackward[n] -= (STEP * i);
                H.put(i, hForward);
                H.put(-i, hBackward);
            }
            Double sum = 0.0;
            for (int i = - numberOfCoefficients + 1; i < numberOfCoefficients; i ++) {
                sum += Math.signum(i) * coefficients[Math.abs(i)] * functionToOptimize.apply(H.get(i));
            }
            res[n] = sum / STEP;
        }
        return res;
    }

    protected Double[] computeNormalizeGradient(Function<Double[], Double> functionToOptimize, Double[] point) {
        return normalize(computeGradient(functionToOptimize, point));
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
