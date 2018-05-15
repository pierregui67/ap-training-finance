package com.qfs.sandbox.forecasting;

import java.util.*;

public class LinearTrendForecast extends ExponentialSmoothingForecast {

    public LinearTrendForecast(ArrayList<Double> values) {
        //super();
        setValues(values);
        evaluateModelParameters(values);
    }

    @Override
    protected void evaluateModelParameters(ArrayList<Double> values) {
        Double[] param = new Double[] {0.9, 0.2};

        GradientDescent gradientDescent = new GradientDescent();
        param = gradientDescent.minimization(this::functionToOptimize, param);

        /*
        Now we compute the level and the trend coefficients associated with the optimal
        parameters.
         */
        ArrayList<Double>[] coefficients = computeForecastCoefficients(values, param);
        setModel(param, coefficients);
    }

    /*
    Setters
     */
    @Override
    protected void setModel(Double[] param, ArrayList<Double>[] coefficients) {
        setModelParameters(param);
        this.levelCoefficients = coefficients[0];
        this.trendCoefficients = coefficients[1];
    }

    @Override
    protected void setModelParameters(Double[] param) {
        alpha = param[0];
        beta = param[1];
    }

    /*
    Compute functions
     */

    protected Double computeLevel(Double y_t, Double l_t, Double b_t) {
        return alpha * y_t + (1 - alpha) * (l_t + b_t);
    }

    protected Double computeTrend(Double l_t1, Double l_t0, Double b_t) {
        return beta * (l_t1 - l_t0) + (1 - beta) * b_t;
    }

    @Override
    protected ArrayList<Double>[] computeForecastCoefficients
            (ArrayList<Double> values, Double[] param) {

        setModelParameters(param);
        int n = values.size();

        /*
        Initialization of the coefficients.
         */
        ArrayList<Double> levelCoefficients = new ArrayList<>();
        levelCoefficients.add(values.get(0));

        ArrayList<Double> trendCoefficients = new ArrayList<>();
        trendCoefficients.add(values.get(1) - values.get(0));

        /*
        Computing all the coefficients.
         */
        for (int i = 1; i < n ; i++) {
            levelCoefficients.add(computeLevel(values.get(i),
                    levelCoefficients.get(i-1), trendCoefficients.get(i-1)));
            trendCoefficients.add(computeTrend(levelCoefficients.get(i),
                    levelCoefficients.get(i-1),
                    trendCoefficients.get(i-1)));
        }

        return new ArrayList[]{levelCoefficients, trendCoefficients};
    }

    /**
    return y_{t+h|t} = l_t + h * b_t
     */
    @Override
    public Double forecast(int h) {
        int len = levelCoefficients.size() - 1;
        Double result = levelCoefficients.get(len) + h * trendCoefficients.get(len);
        return result;
    }


}


