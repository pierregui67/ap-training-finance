package com.qfs.sandbox.forecasting;

import java.util.ArrayList;

public class LinearSeasonalForecast extends ExponentialSmoothingForecast {

    private static Double delta;
    private static int period;

    private static Double computeSeasonal(Double y_t, Double l_t, Double s_ts) {
        return delta * (y_t - l_t) + (1 - delta) * s_ts;
    }

    private static ArrayList<Double> computeSeasonalCoefficients
            (ArrayList<Double> values){
        int n = values.size();
        ArrayList<Double> seasonalCoefficients = new ArrayList<>();
        seasonalCoefficients.add(values.get(0));
        return seasonalCoefficients;
    }
}
