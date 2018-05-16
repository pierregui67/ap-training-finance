package com.qfs.sandbox.forecasting;

import java.util.ArrayList;

public abstract class ExponentialSmoothingModel {

    protected ArrayList<Double> values;

    protected Double alpha, beta;
    protected ArrayList<Double> levelCoefficients, trendCoefficients;

    protected Double modelError;

    protected void setValues(ArrayList<Double> values) {
        this.values = values;
    }

    protected final Double lossFunction(ArrayList<Double> error) {
        Double res = 0.0;
        int len = error.size();
        for (int i = 0; i < len; i++) {
            res += error.get(i) * error.get(i);
        }
        return res;
    }

    protected final Double computeErrorScore(ArrayList<Double>[] coefficients) {
        ArrayList<Double> error = computeError(coefficients);
        return lossFunction(error);
    }

    protected final Double functionToOptimize(Double[] param) {
        ArrayList<Double>[] coefficients = computeForecastCoefficients(values, param);
        ArrayList<Double> error = computeError(coefficients);
        return lossFunction(error);
    }

    protected ArrayList<Double> computeError(ArrayList<Double>[] coefficients) {
        ArrayList<Double> error = new ArrayList<>();
        int len = values.size();
        for (int i = 0; i < len; i++) {
            Double sum = 0.0;
            for (int j = 0; j < coefficients.length; j++)
                sum += coefficients[j].get(i);
            error.add(values.get(i) - sum);
        }
        return error;
    }

    protected abstract void setModelParameters(Double[] param);
    protected abstract void setModel(Double[] param, ArrayList<Double>[] coefficients);

    protected abstract void fitModel();

    protected abstract ArrayList<Double>[] computeForecastCoefficients(ArrayList<Double> values, Double[] param);

    }
