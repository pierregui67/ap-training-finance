package com.qfs.sandbox.forecasting;

import java.util.ArrayList;

public interface Forecast {

    ExponentialSmoothingModel evaluateModel(ArrayList<Double> values);
    Double forecast(int h);
}
