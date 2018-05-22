package com.qfs.sandbox.cfg.pivot.impl;

import com.activeviam.desc.build.ICanStartBuildingMeasures;
import com.activeviam.desc.build.IHasAtLeastOneMeasure;
import com.qfs.sandbox.aggregator.impl.IndicesPriceAggregator;
import com.qfs.sandbox.postprocesseur.impl.*;
import com.quartetfs.biz.pivot.postprocessing.impl.*;

import static com.qfs.sandbox.cfg.pivot.impl.PerformanceCubeDimension.DIM_STOCK_SYMBOL;
import static com.qfs.sandbox.cfg.pivot.impl.PerformanceCubeDimension.HIER_FOREX;
import static com.qfs.sandbox.cfg.pivot.impl.PerformanceCubeDimension.HIER_TIME;
import static com.qfs.sandbox.cfg.pivot.impl.PerformanceCubeManagerConfig.*;
import static com.qfs.sandbox.cfg.role.impl.RoleContextConfig.REF_CURRENCY;

public class PerformanceCubeMeasure {

    /** The folder for sensitivities related measures. */
    private static final String FOLDER_DATA = "data";
    private static final String FOLDER_FOREX = "forex";
    private static final String FOLDER_PP = "post-processeur";
    private static final String FOLDER_HIDDEN = "hidden";
    private static final String FOLDER_FOREX_DISPLAY = "forex-rate";
    private static final String FOLDER_FORECAST = "forecast";

    /*
    Formatter
     */
    /** The double formatter */
    public static final String FORMATTER_DOUBLE = "DOUBLE[#,###.00;-#,###.00]";
    /** The int formatter. */
    public static final String FORMATTER_INT = "INT[#,###]";
    /** The percent formatter */
    public static final String FORMATTER_PERCENT = "DOUBLE[#,##0.00%;-#,##0.00%]";
    /** The forex formatter */
    public static final String FORMATTER_FOREX = "DOUBLE[#,##0.0000;-#,##0.0000]";
    /** The date formatters for timestamps. */
    public static final String FORMATTER_TIMESTAMP = "DATE[HH:mm:ss]";


    /**
     * Adds all the measures to the cube builder.
     *
     * @param builder The builder to enrich with the measures.
     *
     * @return The builder with the new measures.
     */
    public static IHasAtLeastOneMeasure measures(final ICanStartBuildingMeasures builder) {
        return builder
                .withContributorsCount()
                .withAlias("contributors.COUNT")
                .withFormatter(FORMATTER_INT)
                .withUpdateTimestamp()
                .withAlias("Timestamp")
                .withFormatter(FORMATTER_TIMESTAMP)
                .withMeasures(PerformanceCubeMeasure::coreMeasures);
    }

    /**
     * Adds sensitivities related measures to a builder.
     *
     * @param builder The builder to enrich with the measures.
     *
     * @return The builder with the new measures.
     */
    protected static IHasAtLeastOneMeasure coreMeasures(final ICanStartBuildingMeasures builder) {
        return builder

                /*
                Basic Measures
                 */
                .withAggregatedMeasure()
                .sum(CLOSE)
                .withName("_Close.SUM")
                .hidden()


                .withPostProcessor("_Close.INDEX_AGGREGATION")
                .withPluginKey(PriceAggregationPostProcessor.PLUGIN_KEY)
                .withProperty(PriceAggregationPostProcessor.AGGREGATION_FUNCTION, IndicesPriceAggregator.KEY)
                .withUnderlyingMeasures("_Close.SUM")
                .withProperty(PriceAggregationPostProcessor.LEAF_LEVELS,
                        "StockSymbol@StockSymbol@StockSymbol, IndexName@IndexName@IndexName")
                .hidden()


                .withAggregatedMeasure()
                .avg(CLOSE)
                .withName("_Close.AVG")
                .hidden()

                .withAggregatedMeasure()
                .sum(VOLUME)
                .withName("Volume.SUM")
                .withinFolder(FOLDER_DATA)
                .withFormatter(FORMATTER_DOUBLE)

                .withAggregatedMeasure()
                .sum(NUMBER_STOCKS)
                .withName("NumberStocks.SUM")
                .withinFolder(FOLDER_DATA)
                .withFormatter(FORMATTER_DOUBLE)


                /*
                Forex Measures :
                The currency conversion  is carried out only on the the basic values.
                Thus only the Forex's values will have an impact on the following computation.
                 */
                .withPostProcessor("_Forex.SUM")
                .withPluginKey(ForexPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("_Close.INDEX_AGGREGATION")
                .withProperty(ADynamicAggregationPostProcessor.LEAF_LEVELS,
                        "StockSymbol@StockSymbol@StockSymbol")
                .withProperty(ForexPostProcessor.REFERENCE_CURRENCY, "EUR")
                .hidden()

                // The prices should not be aggregated when corresponding to different products
                // and different indices !
                .withPostProcessor("Forex.SUM")
                .withPluginKey(MinimumDepthPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("_Forex.SUM")
                .withinFolder(FOLDER_FOREX)
                .withProperty(MinimumDepthPostProcessor.HIERARCHY_NAME, DIM_STOCK_SYMBOL)
                .withProperty(MinimumDepthPostProcessor.MINIMUM_DEPTH, 1)
                .withFormatter(FORMATTER_DOUBLE)

                // It is accepted to average price of different products
                .withPostProcessor("Forex.AVG")
                .withPluginKey(ForexPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("_Close.AVG")
                .withinFolder(FOLDER_FOREX)
                .withProperty(ADynamicAggregationPostProcessor.LEAF_LEVELS,
                        "StockSymbol@StockSymbol@StockSymbol")
                .withProperty(ForexPostProcessor.REFERENCE_CURRENCY, "EUR")
                .withFormatter(FORMATTER_DOUBLE)


                /*
                Post-Processeur Measures
                 */
                .withPostProcessor("PortfolioValue")
                .withPluginKey(PortfolioValuePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("NumberStocks.SUM", "Forex.AVG")
                .withProperty(PortfolioValuePostProcessor.AGGREGATION_FUNCTION, "SUM")
                .withProperty(PortfolioValuePostProcessor.LEAF_LEVELS,
                        "StockSymbol@StockSymbol@StockSymbol")
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("PortfolioValueWeights")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "aggregatedValue[PortfolioValue],aggregatedValue[TotalPortfolioValue],int[0],div")
                .withFormatter(FORMATTER_PERCENT)
                .withinFolder(FOLDER_PP)

                /*
                Total Portfolio Value :
                We compute the TotalPortfolioValue regarding two dimensions : StockSymbol and CompaniesRegroupement
                Thus we combine two TOTAL_PORTFOLIO_VALUE post-processors. That suit well.
                However it is not optimized nor an intelligent design.
                TODO : One should better recode the TOTAL_PORTFOLIO_VALUE post-processor
                TODO : so as to accept a list of level to shift !
                 */
                .withPostProcessor("_TotalPortfolioValue")
                .withPluginKey(TotalPortfolioValuePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("PortfolioValue")
                .withProperty(TotalPortfolioValuePostProcessor.LEVEL_TO_SHIFT,
                        "Name@CompaniesRegroupment@CompaniesRegroupment")
                .hidden()

                .withPostProcessor("TotalPortfolioValue")
                .withPluginKey(TotalPortfolioValuePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("_TotalPortfolioValue")
                .withProperty(TotalPortfolioValuePostProcessor.LEVEL_TO_SHIFT,
                        "StockSymbol@StockSymbol@StockSymbol")
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_HIDDEN)

                /*
                Period Return
                 */
                .withPostProcessor("PeriodReturn")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "aggregatedValue[PortfolioValue],aggregatedValue[PreviousPortfolioValue],-")
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("PeriodReturnPercent")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "(aggregatedValue[PeriodReturn],aggregatedValue[PreviousPortfolioValue],int[0],div)")
                .withFormatter(FORMATTER_PERCENT)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("PreviousPortfolioValue")
                .withPluginKey(PreviousValuePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(NeighborValuePostProcessor.TIME_HIERARCHY_PROPERTY, HIER_TIME)
                .withProperty(NeighborValuePostProcessor.STREAM_MEASURE_PROPERTY, "PortfolioValue")
                .withProperty(NeighborValuePostProcessor.DIRECTION_PROPERTY, "next")
                .withProperty("memberToRemove", 0)
                .withProperty("reverseDimensionOrder", false)
                .withinFolder(FOLDER_HIDDEN)

                /*
                VaR
                 */
                .withPostProcessor("Var")
                .withPluginKey(VaRPostProcessor.PLUGIN_KEY)
                .withUnderlyingMeasures("VarBucketer, PortfolioValue")
                .withProperty(VaRPostProcessor.LEAF_LEVELS, "StockSymbol@StockSymbol@StockSymbol")
                .withFormatter(FORMATTER_PERCENT)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("VarBucketer")
                .withPluginKey(VaRBucketerPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(VaRBucketerPostProcessor.TIME_HIERARCHY_PROPERTY, HIER_TIME)
                .withProperty(NeighborValuePostProcessor.STREAM_MEASURE_PROPERTY, "PeriodReturn")
                .withProperty(NeighborValuePostProcessor.DIRECTION_PROPERTY, "next")
                .withinFolder(FOLDER_HIDDEN)



                /*
                Cumulative Return
                 */
                .withPostProcessor("CumulativeReturn")
                .withPluginKey(Stream2PositionPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(Stream2PositionPostProcessor.TIME_HIERARCHY_PROPERTY, HIER_TIME)
                .withProperty(Stream2PositionPostProcessor.STREAM_MEASURE_PROPERTY, "PeriodReturn")
                .withProperty(Stream2PositionPostProcessor.POSITION_TYPE, "CURRENT_STREAM")
                // TODO : add reverseDimensionorder ?
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("CumulativeReturnPercent")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "((aggregatedValue[PortfolioValue], aggregatedValue[InitialValue], -),aggregatedValue[InitialValue],int[0],div)")
                .withFormatter(FORMATTER_PERCENT)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("InitialValue")
                .withPluginKey(InitialValuePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(InitialValuePostProcessor.TIME_HIERARCHY_PROPERTY, HIER_TIME)
                .withProperty(InitialValuePostProcessor.STREAM_MEASURE_PROPERTY, "PortfolioValue")
                .withProperty("memberToRemove", "0")
                .withProperty("reverseDimensionOrder", "false")
                // TODO : add memberToRemove and reverseDimensionOrder ?
                .withinFolder(FOLDER_HIDDEN)


                .withPostProcessor("CompareExample2Portfolio")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "aggregatedValue[ExamplePV],aggregatedValue[BenchmarkPV],-")
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("CompareExample2PortfolioPercent")
                .withPluginKey(FormulaPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(FormulaPostProcessor.FORMULA_PROPERTY,
                        "(aggregatedValue[CompareExample2Portfolio],aggregatedValue[BenchmarkPV],int[0],div)")
                .withFormatter(FORMATTER_DOUBLE)
                .withinFolder(FOLDER_PP)

                .withPostProcessor("ExamplePV")
                .withPluginKey(ChoosePVTypePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("PortfolioValue")
                .withProperty(ChoosePVTypePostProcessor.PORTFOLIO_TYPE, "exemple")
                .withProperty(ChoosePVTypePostProcessor.HIERARCHY_NAME, "IndexName")
                .withProperty(ChoosePVTypePostProcessor.LEVEL_TO_SHIFT, "IndexName@IndexName@IndexName")
                .withinFolder(FOLDER_HIDDEN)

                .withPostProcessor("BenchmarkPV")
                .withPluginKey(ChoosePVTypePostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withUnderlyingMeasures("PortfolioValue")
                .withProperty(ChoosePVTypePostProcessor.PORTFOLIO_TYPE, "benchmark")
                .withProperty(ChoosePVTypePostProcessor.HIERARCHY_NAME, "IndexName")
                .withProperty(ChoosePVTypePostProcessor.LEVEL_TO_SHIFT, "IndexName@IndexName@IndexName")
                .withinFolder(FOLDER_HIDDEN)

                /*
                Display Forex
                 */
                .withPostProcessor("ForexRate")
                .withPluginKey(ForexDisplayPostProcessor.PLUGIN_KEY)
                .withContinuousQueryHandlers("STORED", ForexDisplayHandler.PLUGIN_KEY)
                .withProperty(ForexDisplayPostProcessor.HIER_FOREX, HIER_FOREX)
                .withProperty(ForexDisplayPostProcessor.REFERENCE_CURRENCY, REF_CURRENCY)
                .withProperty(ForexDisplayPostProcessor.ANALYSIS_LEVELS_PROPERTY,
                        "Currency@ForexHier@ForexDim")
                .withFormatter(FORMATTER_FOREX)
                .withinFolder(FOLDER_FOREX_DISPLAY)

                /*
                Forecast
                 */
                .withPostProcessor("ForecastValue")
                .withPluginKey(ForecastPostProcessor.PLUGIN_KEY)
                .withUnderlyingMeasures("Forex.SUM")
                .withContinuousQueryHandlers("STORED", ForexHandler.PLUGIN_KEY)
                .withProperty(ForecastPostProcessor.STOCK_SYMBOL_LEVEL,
                        "StockSymbol@StockSymbol@StockSymbol")
                .withProperty(ForecastPostProcessor.LEAF_LEVELS,
                        "StockSymbol@StockSymbol@StockSymbol")//"Date@HistoricalDates@Time,
                // The ANALYSIS_LEVEL property is very important
                .withProperty(ForecastPostProcessor.ANALYSIS_LEVELS,
                        "Date@ForecastHier@Forecast")
                .withinFolder(FOLDER_FORECAST)
                .withFormatter(FORMATTER_DOUBLE)

                ;
    }
}
