package com.qfs.sandbox.pivot.impl;

import com.activeviam.desc.build.ICanStartBuildingMeasures;
import com.activeviam.desc.build.IHasAtLeastOneMeasure;

import static com.qfs.sandbox.pivot.impl.PerformanceCubeConfig.DATA_FOLDER;

public class PerformanceCubeMeasure {

    /*
    Measures
     */

    public static final String VOLUME = "Volume";
    public static final String NUMBER_STOCKS = "NumberStocks";
    /*
    Formatter
     */
    /** The double formatter */
    public static final String DOUBLE_FORMATTER = "DOUBLE[#,###;-#,###]";
    /** The int formatter. */
    public static final String INT_FORMATTER = "INT[#,###]";
    /** The date formatters for timestamps. */
    public static final String TIMESTAMP_FORMATTER = "DATE[HH:mm:ss]";

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
                .withAlias("Count")
                .withFormatter(INT_FORMATTER)
                .withUpdateTimestamp()
                .withAlias("Timestamp")
                .withFormatter(TIMESTAMP_FORMATTER)
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
                .withAggregatedMeasure()
                .sum(VOLUME)
                .withinFolder(DATA_FOLDER)
                .withFormatter(DOUBLE_FORMATTER)

                .withAggregatedMeasure()
                .sum(NUMBER_STOCKS)
                .withinFolder(DATA_FOLDER)
                .withFormatter(DOUBLE_FORMATTER);
    }

}
