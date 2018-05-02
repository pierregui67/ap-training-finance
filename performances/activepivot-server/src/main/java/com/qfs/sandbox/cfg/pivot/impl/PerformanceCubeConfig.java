package com.qfs.sandbox.cfg.pivot.impl;

import com.activeviam.builders.StartBuilding;
import com.activeviam.desc.build.ICanBuildCubeDescription;
import com.activeviam.desc.build.ICubeDescriptionBuilder;
import com.quartetfs.biz.pivot.context.impl.QueriesTimeLimit;
import com.quartetfs.biz.pivot.definitions.IActivePivotInstanceDescription;

public class PerformanceCubeConfig {

    /** The name of the cube */
    public static final String CUBE_NAME = "PerformanceAttributionCube";



    /**
     * Creates the cube description.
     *
     * @param isActiveMonitorEnabled Whether ActiveMonitor is enabled or not.
     * @return The created cube description
     */
    public static IActivePivotInstanceDescription createCubeDescription(final boolean isActiveMonitorEnabled) {
        return configureCubeBuilder(StartBuilding.cube(CUBE_NAME), isActiveMonitorEnabled).build();
    }

    /**
     * Configures the given builder in order to created the cube
     * description.
     *
     * @param builder The builder to configure
     * @param isActiveMonitorEnabled Whether ActiveMonitor is enabled or not.
     * @return The configured builder
     */
    public static ICanBuildCubeDescription<IActivePivotInstanceDescription> configureCubeBuilder(
            final ICubeDescriptionBuilder.INamedCubeDescriptionBuilder builder,
            final boolean isActiveMonitorEnabled)
    {
        return builder
                .withMeasures(PerformanceCubeMeasure::measures)
                .withDimensions(PerformanceCubeDimension::dimensions)
                /*.withAggregateProvider()
                .withPartialProvider()
                .excludingHierarchies(new HierarchyCoordinate(TRADES_HIERARCHY))
                .includingOnlyMeasures(PNL_DELTA_SUM, PNL_SUM)
                .withPartialProvider()
                .includingOnlyHierarchies(
                        new HierarchyCoordinate(UNDERLYINGS_HIERARCHY),
                        new HierarchyCoordinate(TIME_DIMENSION, HISTORICAL_DATES_HIERARCHY))
                .withPartialProvider()
                .excludingMeasures(PNL_DELTA_SUM, PNL_SUM)*/

                /*.withDrillthroughExecutor()
                .withKey(TimeBucketDrillthroughExecutor.PLUGIN_KEY)
                .withProperties(
                        TimeBucketLocationInterpreter.BUCKET_HIERARCHY_PROPERTY, TIME_BUCKET_DYNAMIC_HIERARCHY,
                        TimeBucketLocationInterpreter.BUCKETED_LEVEL_PROPERTY, VALUE_DATE_LEVEL)

                .withAggregatesCache()
                .withSize(1_000)
                .cachingOnlyMeasures(IMeasureHierarchy.COUNT_ID, PNL_SUM)*/

                // Shared context values
                //.withSharedContextValue(drillthroughProperties())
                //.withSharedContextValue(mdxContext(isActiveMonitorEnabled))
                // Query maximum execution time (before timeout cancellation): 30s
                .withSharedContextValue(new QueriesTimeLimit(80))
                ;
    }
}
