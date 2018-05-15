package com.qfs.sandbox.forecasting;

import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class LinearSeasonalForecast extends ExponentialSmoothingForecast {

    private Double delta;
    private int period;

    private final int MIN_PERIOD = 2;
    private final int MAX_PERIOD = 30;

    protected ArrayList<Double> seasonalCoefficients;

    public LinearSeasonalForecast(ArrayList<Double> values) {
        //period = 14;
        setValues(values);
        evaluateModelParameters(values);
    }

    private Double computeSeasonal(Double y_t, Double l_t, Double b_t, Double s_tm) {
        return delta * (1 - alpha) * (y_t - l_t - b_t) +
                (1 - delta * (1 - alpha)) * s_tm;
    }

    protected Double computeLevel(Double y_t, Double l_t, Double b_t, Double s_tm) {
        return alpha * (y_t - s_tm) + (1 - alpha) * (l_t + b_t);
    }

    protected Double computeTrend(Double l_t1, Double l_t0, Double b_t) {
        return beta * (l_t1 - l_t0) + (1 - beta) * b_t;
    }

    @Override
    ArrayList<Double>[] computeForecastCoefficients(ArrayList<Double> values, Double[] param) {
        setModelParameters(param);
        int n = values.size();

        /*
        Initialization of the coefficients.
         */
        ArrayList<Double> levelCoefficients = new ArrayList<>();
        Double l0 = 0.0;
        for (int i = 0; i < period; i++)
            l0 += values.get(i);
        l0 /= period;
        levelCoefficients.add(l0);

        ArrayList<Double> trendCoefficients = new ArrayList<>();
        Double t0 = 0.0;
        for (int i = 0; i < period; i++)
            t0 += (values.get(i+1) - values.get(i)) / period;
        t0 /= period;
        trendCoefficients.add(t0);

        ArrayList<Double> seasonalCoefficients = new ArrayList<>();
        for (int i = 0; i < period; i++)
            seasonalCoefficients.add(values.get(i) - l0);

        /*
        Computing all the coefficients.
         */
        for (int i = 1; i < n ; i++) {
            Double y_t = values.get(i);
            levelCoefficients.add(computeLevel(y_t,
                    levelCoefficients.get(i-1),
                    trendCoefficients.get(i-1),
                    seasonalCoefficients.get(i - period + period)));
            trendCoefficients.add(computeTrend(levelCoefficients.get(i),
                    levelCoefficients.get(i-1),
                    trendCoefficients.get(i-1)));
            seasonalCoefficients.add(computeSeasonal(y_t,
                    levelCoefficients.get(i-1),
                    trendCoefficients.get(i-1),
                    seasonalCoefficients.get(i - period + period)));
        }

        return new ArrayList[]{levelCoefficients,
                trendCoefficients, seasonalCoefficients};
    }

    @Override
    protected void evaluateModelParameters(ArrayList<Double> values) {

        HashMap<Integer, Double[]> errorToParam = new HashMap<>();
        Double minError = Double.MAX_VALUE;
        int minPeriod = MIN_PERIOD;

        Double[] param;
        for (int p = MIN_PERIOD; p < MAX_PERIOD; p++){
            this.period = p;
            param = new Double[] {0.9, 0.2, 0.5};

            GradientDescent gradientDescent = new GradientDescent();
            param = gradientDescent.minimization(this::functionToOptimize, param);
            ArrayList<Double>[] coefficients = computeForecastCoefficients(values, param);
            Double error = computeErrorScore(coefficients);
            if (error < minError) {
                minError = error;
                minPeriod = p;
            }
            errorToParam.put(p, param);
        }


        /*
        Now we compute the level and the trend coefficients associated with the optimal
        parameters.
         */
        param = errorToParam.get(minPeriod);
        period = minPeriod;
        ArrayList<Double>[] coefficients = computeForecastCoefficients(values, param);
        setModel(param, coefficients);
    }

    @Override
    protected void setModelParameters(Double[] param) {
        alpha = param[0];
        beta = param[1];
        delta = param[2];
    }

    @Override
    protected void setModel(Double[] param, ArrayList<Double>[] coefficients) {
        setModelParameters(param);
        levelCoefficients = coefficients[0];
        trendCoefficients = coefficients[1];
        seasonalCoefficients = coefficients[2];
    }

    /**
     return y_{t+h|t} = l_t + h * b_t + s_tm
     */
    @Override
    public Double forecast(int h) {
        int len = levelCoefficients.size() - 1;
        // TODO : check it's correct.
        int hm = Math.floorMod(h-1, period) + 1;
        Double result = levelCoefficients.get(len) + h * trendCoefficients.get(len) +
                seasonalCoefficients.get(len - period + hm);
        return result;
    }
}
