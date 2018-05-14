package com.qfs.sandbox.forecasting;

import javafx.util.Pair;
import java.util.*;

public class LinearTrendForecast extends ExponentialSmoothingForecast {

    public LinearTrendForecast(ArrayList<Double> values) {
        setValues(values);
        evaluateModelParameters(values);
    }

    protected void evaluateModelParameters(ArrayList<Double> values) {
        Double[] param = new Double[] {0.1, 0.9};
        /*ParameterOptimization parameterOptimization = new ParameterOptimization(
                param, values);*/

        GradientDescent gradientDescent = new GradientDescent();
        param = gradientDescent.minimization(this::functionToOptimize, param);

        /*
        Now we compute the level and the trend coefficients associated with the optimal
        parameters.
         */
        Pair<ArrayList<Double>, ArrayList<Double>> pair = computeTrendCoefficients(values, param);
        ArrayList<Double> levelCoefficients = pair.getKey();
        ArrayList<Double> trendCoefficients = pair.getValue();
        setModel(param, levelCoefficients, trendCoefficients);
    }

    /*
    Setters
     */
    private void setModel(Double[] param, ArrayList<Double> levelCoefficients,
                          ArrayList<Double> trendCoefficients) {
        /*alpha = 0.9;
        beta = 0.2;*/
        setModelParameters(param);
        this.levelCoefficients = levelCoefficients;
        this.trendCoefficients = trendCoefficients;

    }

    private void setModelParameters(Double[] param) {
        alpha = param[0];
        beta = param[1];
    }

    /*
    Compute functions
     */

    protected Double computeLevel(Double y_t, Double l_t, Double b_t) {
        return alpha * y_t + (1 - alpha) * (l_t + b_t);
    }

    private Double computeTrend(Double l_t1, Double l_t0, Double b_t) {
        return beta * (l_t1 - l_t0) + (1 - beta) * b_t;
    }

    protected ArrayList<Double> computeError(ArrayList<Double> values,
                                                    ArrayList<Double> levelCoefficients,
                                                    ArrayList<Double> trendCoefficients) {
        ArrayList<Double> error = new ArrayList<>();
        int len = values.size();
        for (int i = 0; i < len; i++) {
            error.add(values.get(i) - (levelCoefficients.get(i) - trendCoefficients.get(i)));
        }
        return error;
    }

    protected Double lossFunction(ArrayList<Double> error) {
        Double res = 0.0;
        int len = error.size();
        for (int i = 0; i < len; i++) {
            res += error.get(i) * error.get(i);
        }
        return res;
    }

    protected Pair< ArrayList<Double>,  ArrayList<Double>> computeTrendCoefficients
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
            levelCoefficients.add(computeLevel(values.get(i), levelCoefficients.get(i-1), trendCoefficients.get(i-1)));
            trendCoefficients.add(computeTrend(levelCoefficients.get(i), levelCoefficients.get(i-1),
                    trendCoefficients.get(i-1)));
        }
        return new Pair<>(levelCoefficients, trendCoefficients);
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

    @Override
    protected Double functionToOptimize(Double[] param) {
        Pair<ArrayList<Double>, ArrayList<Double>> pair = computeTrendCoefficients(values, param);
        ArrayList<Double> levelCoefficients = pair.getKey();
        ArrayList<Double> trendCoefficients = pair.getValue();
        ArrayList<Double> error = computeError(values, levelCoefficients, trendCoefficients);
        return lossFunction(error);
    }

}


