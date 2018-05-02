package com.qfs.sandbox.cfg.pivot.impl;

import com.activeviam.desc.build.ICanBuildCubeDescription;
import com.activeviam.desc.build.dimensions.ICanStartBuildingDimensions;
import com.qfs.fwk.ordering.impl.ReverseEpochComparator;
import com.quartetfs.biz.pivot.cube.dimension.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.definitions.IActivePivotInstanceDescription;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.FOREX_STORE_NAME;
import static com.qfs.sandbox.cfg.pivot.impl.PerformanceCubeManagerConfig.*;
import static com.quartetfs.biz.pivot.cube.hierarchy.IOlapElement.XMLA_DESCRIPTION;

public class PerformanceCubeDimension {

    /* **************************************** */
    /* Levels, hierarchies and dimensions names */
    /* **************************************** */

    public static final String DIM_STOCK_SYMBOL = "StockSymbol";
    public static final String DIM_TIME = "Time";
    public static final String DIM_INDEX_NAME = "IndexName";
    public static final String DIM_FOREX = "ForexDim";
    public static final String DIM_COMPANIES_REGROUPMENT = "CompaniesRegroupment";

    public static final String HIER_TIME = "HistoricalDates";
    public static final String HIER_FOREX = "ForexHier";

    /*public static final String LEV_TIME = "Date";
    public static final String LEV_FOREX = "Currency";
    public static final String LEV_SECTOR = "Sector";
    public static final String LEV_INDUSTRY = "Industry";
    public static final String LEV_NAME = "Name";*/

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
                .withDimension(DIM_COMPANIES_REGROUPMENT)
                .withHierarchyOfSameName()
                .withLevel(SECTOR)
                .withLevel(INDUSTRY)
                .withLevel(NAME)

                .withDimension(DIM_TIME)
                .withType(IDimension.DimensionType.TIME)
                .withHierarchy(HIER_TIME).slicing()
                .withLevel(DATE)
                .withType(ILevelInfo.LevelType.TIME)
                .withFormatter("DATE[yyyy-MM-dd]")

                .withSingleLevelDimensions(DIM_STOCK_SYMBOL, DIM_INDEX_NAME)

                .withDimension(DIM_FOREX)
                .withHierarchy(HIER_FOREX)
                .factless()
                .withStoreName(FOREX_STORE_NAME)
                .withLevel("Currency")
                .withFieldName("TargetCurrency")

                /*.withEpochDimension()
                .withEpochsLevel()
                .withComparator(ReverseEpochComparator.TYPE)
                .withFormatter("EPOCH[HH:mm:ss]")
                .end()*/
                ;
    }
}
