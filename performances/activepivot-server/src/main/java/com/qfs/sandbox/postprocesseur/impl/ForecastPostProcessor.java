package com.qfs.sandbox.postprocesseur.impl;

import com.qfs.condition.impl.BaseConditions;
import com.qfs.store.query.ICursor;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.IIterableAggregatesRetrievalResult;
import com.quartetfs.biz.pivot.cellset.ITransformProcedure;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import javafx.util.Pair;

import java.util.*;

import static com.qfs.sandbox.cfg.analysehier.impl.ForecastHierarchy.EXTRA_DATES_CARDINAL;
import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.*;
import static com.qfs.sandbox.forecasting.LinearTrendForecast.computeTrendCoefficients;
import static com.qfs.sandbox.forecasting.LinearTrendForecast.forecast;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForecastPostProcessor.PLUGIN_KEY)
public class ForecastPostProcessor extends ADynamicAggregationPostProcessor<Double, Double> {

    private static HashMap<Date, Double> dates = new HashMap<>();

    public static final String PLUGIN_KEY = "ETS_FORECAST";

    public static final String DATE_LEVEL = "confirmedDateLevel";
    public static final String ANALYSIS_LEVELS = "analysisLevels";
    public static final String STOCK_SYMBOL_LEVEL = "stockSymbol";

    Date maxDate;
    HashMap<String, TreeMap<Date, Double>> allTheDatesForAllStockSymbol;

    private ILevel dateLevel;
    private IHierarchy dateHier;

    private ILevel stockSymLevel;
    private IHierarchy stockSymHier;

    private ArrayList<ILevel> analysisLevels = new ArrayList<>();
    private ArrayList<IHierarchy> analysisHiers = new ArrayList<>();

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public ForecastPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        if (properties.containsKey(DATE_LEVEL)) {
            String levelName = properties.getProperty(DATE_LEVEL).split("@")[0];
            String hierName = properties.getProperty(DATE_LEVEL).split("@")[1];
            String dimName = properties.getProperty(DATE_LEVEL).split("@")[2];
            dateLevel = HierarchiesUtil.getLevel(pivot, dimName, hierName, levelName);
            dateHier = HierarchiesUtil.getHierarchy(pivot, dimName, hierName);
        } else {
            //throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + DATE_LEVEL);
        }

        if (properties.containsKey(STOCK_SYMBOL_LEVEL)) {
            String levelName = properties.getProperty(STOCK_SYMBOL_LEVEL).split("@")[0];
            String hierName = properties.getProperty(STOCK_SYMBOL_LEVEL).split("@")[1];
            String dimName = properties.getProperty(STOCK_SYMBOL_LEVEL).split("@")[2];
            stockSymLevel = HierarchiesUtil.getLevel(pivot, dimName, hierName, levelName);
            stockSymHier = HierarchiesUtil.getHierarchy(pivot, dimName, hierName);
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + STOCK_SYMBOL_LEVEL);
        }

        if (properties.containsKey(ANALYSIS_LEVELS)) {
            String[] analysisString = properties.getProperty(ANALYSIS_LEVELS).split(",");
            for (String s : analysisString) {
                String levelName = s.split("@")[0];
                String hierName = s.split("@")[1];
                String dimName = s.split("@")[2];
                analysisLevels.add(HierarchiesUtil.getLevel(pivot, dimName, hierName, levelName));
                analysisHiers.add(HierarchiesUtil.getHierarchy(pivot, dimName, hierName));
            }
        } else {
            //throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + ANALYSIS_LEVELS);
        }
    }

    @Override
    protected ITransformProcedure createLeafEvaluationProcedure(
            final IIterableAggregatesRetrievalResult result,
            final int[] measureIds)
    {
        // TODO : do not forecast all the stock symbol but only those in the view.
        allTheDatesForAllStockSymbol = new HashMap<>();
        return super.createLeafEvaluationProcedure(result, measureIds);
    }


    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Date dateForecast = (Date) LocationUtil.getCoordinate(leafLocation, analysisLevels.get(0).getLevelInfo());

        String stockSymbolInTheView = (String) LocationUtil.getCoordinate(leafLocation, stockSymLevel.getLevelInfo());

        if ( ! allTheDatesForAllStockSymbol.containsKey(stockSymbolInTheView)) {
            carryForecast(leafLocation, stockSymbolInTheView);
        }
        TreeMap<Date, Double> allTheDates = allTheDatesForAllStockSymbol.get(stockSymbolInTheView);
        if (allTheDates.keySet().contains(dateForecast)) {
            Double value = allTheDates.get(dateForecast);
            allTheDates.remove(dateForecast);
            return value;
        }
        return null;
    }

    private void carryForecast(ILocation location, String stockSymbolInTheView) {

        TreeMap<Date, Double> allTheDates = new TreeMap();

        /*
        Getting all the dates for which we have the close value of the corresponding
        stock symbols.
        */
        ICursor cursor = getDatastoreVersion().getQueryRunner()
                .forStore(STOCK_PRICE_HISTORY_STORE_NAME)
                .withCondition(BaseConditions.Equal(HISTORY_STOCK_SYMBOL, stockSymbolInTheView))
                .selecting(HISTORY_DATE, HISTORY_CLOSE)
                .run();
        while (cursor.hasNext()) {
            cursor.next();
            IRecordReader reader = cursor.getRecord();
            allTheDates.put((Date) reader.read(HISTORY_DATE),
                    (Double) reader.read(HISTORY_CLOSE));
        }

        /*
         Correcting data
          */
        Double previousV = null, currentV = null;
        Date currentD = null;
        for (Map.Entry<Date, Double> entry : allTheDates.entrySet()) {
            Double nextV = entry.getValue();
            Date nextD = entry.getKey();
            if (currentV == null) {
                currentV = nextV;
            } else if (previousV != null && currentV == 0.0) {
                allTheDates.replace(currentD, (previousV + nextV) / 2);
            }
            previousV = currentV;
            currentV = nextV;
            currentD = nextD;
        }

        /*
        Forecast data
         */
        // TODO : Here the model must be fitted.
        ArrayList<Double> values = new ArrayList<>(allTheDates.values());
        Pair<ArrayList<Double>, ArrayList<Double>> pair = computeTrendCoefficients(values);
        ArrayList<Double> levelCoefficients = pair.getKey();
        ArrayList<Double> trendCoefficients = pair.getValue();

        maxDate = allTheDates.lastKey();
        // Beginning the forecast
        Date date = maxDate;
        if (date != null) {
            for (int i = 1; i < EXTRA_DATES_CARDINAL; i++) {
                // TODO : Here the value must be forecasted.
                Double forecastValue = forecast(levelCoefficients, trendCoefficients, i);

                allTheDates.put(date, forecastValue);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DATE, 1);
                date = c.getTime();
            }
            allTheDates.put(date, 0.0);
        }
        allTheDatesForAllStockSymbol.put(stockSymbolInTheView, allTheDates);
    }
}
