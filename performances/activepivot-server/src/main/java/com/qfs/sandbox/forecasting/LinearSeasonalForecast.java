package com.qfs.sandbox.forecasting;


import java.util.ArrayList;
import java.util.concurrent.*;

public class LinearSeasonalForecast implements Forecast {

    LinearSeasonalModel model;

    protected final int MIN_PERIOD = 2;
    protected final int MAX_PERIOD = 30;

    public LinearSeasonalForecast(ArrayList<Double> values) {
        this.model = evaluateModel(values);
    }

    /**
     * return y_{t+h|t} = l_t + h * b_t + s_tm
     */
    @Override
    public Double forecast(int h) {
        int len = model.levelCoefficients.size() - 1;
        // TODO : check it's correct.
        int hm = Math.floorMod(h - 1, model.period) + 1;
        Double result = model.levelCoefficients.get(len) + h * model.trendCoefficients.get(len) +
                model.seasonalCoefficients.get(len - model.period + hm);
        return result;
    }

    @Override
    public LinearSeasonalModel evaluateModel(ArrayList<Double> values) {

        Double minError = Double.MAX_VALUE;
        LinearSeasonalModel bestModel = null;

        for (int p = MIN_PERIOD; p < MAX_PERIOD; p++) {
            LinearSeasonalModel model = new LinearSeasonalModel(values, p);
            ExecutorService executor = Executors.newCachedThreadPool();
            Future<LinearSeasonalModel> futureCall = executor.submit(new LinearSeasonalForecast.ParallelEvaluation(model));
            try {
                model = futureCall.get(); // Here the thread will be blocked
                if (model.modelError < minError) {
                    minError = model.modelError;
                    bestModel = model;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return bestModel;
    }

    class ParallelEvaluation implements Callable<LinearSeasonalModel> {

        LinearSeasonalModel model;

        public ParallelEvaluation(LinearSeasonalModel model) {
            this.model = model;
        }

        @Override
        public LinearSeasonalModel call() throws Exception {
            model.fitModel();
            return model;
        }
    }
}
