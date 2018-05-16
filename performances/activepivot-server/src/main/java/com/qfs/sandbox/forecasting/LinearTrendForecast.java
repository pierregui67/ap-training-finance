package com.qfs.sandbox.forecasting;

import java.util.ArrayList;

public class LinearTrendForecast implements Forecast {

    LinearTrendModel model;

    public LinearTrendForecast(ArrayList<Double> values) {
        this.model = evaluateModel(values);
    }

    @Override
    public LinearTrendModel evaluateModel(ArrayList<Double> values) {
        LinearTrendModel model = new LinearTrendModel(values);
        model.fitModel();
        return model;
    }

    /**
     return y_{t+h|t} = l_t + h * b_t
     */
    @Override
    public Double forecast(int h) {
        int len = model.levelCoefficients.size() - 1;
        Double result = model.levelCoefficients.get(len) +
                h * model.trendCoefficients.get(len);
        return result;
    }
}
