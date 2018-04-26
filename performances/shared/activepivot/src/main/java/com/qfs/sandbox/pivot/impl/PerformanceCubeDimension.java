package com.qfs.sandbox.pivot.impl;

import com.activeviam.desc.build.ICanBuildCubeDescription;
import com.activeviam.desc.build.dimensions.ICanStartBuildingDimensions;
import com.qfs.fwk.ordering.impl.ReverseEpochComparator;
import com.quartetfs.biz.pivot.definitions.IActivePivotInstanceDescription;

import static com.quartetfs.biz.pivot.cube.hierarchy.IOlapElement.XMLA_DESCRIPTION;

public class PerformanceCubeDimension {

    /* **************************************** */
    /* Levels, hierarchies and dimensions names */
    /* **************************************** */

    public static final String DIMENSION_STOCK_SYMBOL = "StockSymbol";

    /**
     * Adds the dimensions descriptions to the input
     * builder.
     *
     * @param builder The cube builder
     * @return The builder for chained calls
     */
    public static ICanBuildCubeDescription<IActivePivotInstanceDescription> dimensions(
            ICanStartBuildingDimensions builder)
    {
        return builder
                .withDimension(DIMENSION_STOCK_SYMBOL)
                .withProperty(XMLA_DESCRIPTION, "Dimension of stock symbol")
                .withHierarchyOfSameName()
                .withLevelOfSameName()

                //.withSingleLevelDimension(DIMENSION_STOCK_SYMBOL)

                .withEpochDimension()
                .withEpochsLevel()
                .withComparator(ReverseEpochComparator.TYPE)
                .withFormatter("EPOCH[HH:mm:ss]")
                .end()
                ;
    }
}
