package com.qfs.sandbox.forecasting;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class LinearSeasonalForecast implements Forecast {

    LinearSeasonalModel model;

    protected final int MIN_PERIOD = 2;
    protected final int MAX_PERIOD = 45;

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

        long startTime = System.currentTimeMillis();

        Set<Callable<LinearSeasonalModel>> callables = new HashSet<>();

        for (int p = MIN_PERIOD; p < MAX_PERIOD; p++) {
            LinearSeasonalModel model = new LinearSeasonalModel(values, p);
            callables.add(new ParallelEvaluation(model));
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Future<LinearSeasonalModel>> futures = executorService.invokeAll(callables);
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
            for (Future<LinearSeasonalModel> future : futures) {
                model = future.get();
                if (model.modelError < minError) {
                    minError = model.modelError;
                    bestModel = model;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Elapsed time = " + estimatedTime);
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
