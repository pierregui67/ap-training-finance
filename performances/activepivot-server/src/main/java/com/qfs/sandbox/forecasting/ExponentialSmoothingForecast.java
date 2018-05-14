package com.qfs.sandbox.forecasting;

import java.util.ArrayList;

public abstract class ExponentialSmoothingForecast {

    protected Double alpha, beta;
    protected ArrayList<Double> levelCoefficients, trendCoefficients;

    protected ArrayList<Double> values;

    public void setValues(ArrayList<Double> values) {
        this.values = values;
    }

    protected abstract Double functionToOptimize(Double[] param);
    public abstract Double forecast(int h);

}
