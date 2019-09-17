package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationDiscriminator;
import com.quartetfs.biz.pivot.IPointLocationReader;
import com.quartetfs.biz.pivot.cellset.IAggregatesRetrievalResult;
import com.quartetfs.biz.pivot.cellset.IPointProcedure;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IMeasuresProvider;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AAdvancedPostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAdvancedAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.*;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = DynamicPeformance.PLUGIN_KEY)
public class DynamicPeformance extends AAdvancedPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PERFORMANCE";
    protected IHierarchyInfo levelToShift;
    protected static Collection<ILocation> locations = new ArrayList<>();
    protected String[] targetMeasures;

    public DynamicPeformance(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(), properties.getProperty("levelToShift"));
        IHierarchy test = getActivePivot().getHierarchies().get(1);
        int ordinal = test.getOrdinal()-1;
        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getHierarchyInfo();

        // Prefetcher of the read cell set
        final IMeasuresProvider measuresProvider = getMeasuresProvider();
        this.prefetchers.add(new LocationShiftPrefetcher(measuresProvider, "benchmark", levelToShift, this.underlyingMeasures));
    }

    @Override
    public void compute(ILocation location2compare, IAdvancedAggregatesRetriever retriever) {
        // Récupère les aggrégats de la location demandée

        ILocation locationBenchmark = LocationUtil.createModifiedLocation(location2compare, levelToShift, new Object[] {ILevel.ALLMEMBER, "benchmark"});

        IAggregatesRetrievalResult aggregats2compare = retriever.retrieveAggregates(location2compare, underlyingMeasures);
        IAggregatesRetrievalResult aggregatsBenchmark = retriever.retrieveAggregates(locationBenchmark, underlyingMeasures);

        // Itère sur les aggrégats et écrit le résultat sur chaque point location
        int measureId2compare = aggregats2compare.getMeasureId(underlyingMeasures[0]);
        int measureIdBench = aggregatsBenchmark.getMeasureId(underlyingMeasures[0]);

        aggregats2compare.forEachPoint(new IPointProcedure() {
            @Override
            public boolean execute(IPointLocationReader pointLocation, int pointId) {
                Double value2compare = aggregats2compare.readDouble(pointId, measureId2compare);
                ILocation pointLocationBenchmark = LocationUtil.createModifiedLocation(pointLocation, levelToShift, new Object[] {ILevel.ALLMEMBER, "benchmark"});
                int pointIdBench = aggregatsBenchmark.getRow(pointLocationBenchmark);
                if(pointIdBench == -1){
                    retriever.write(pointLocation, -value2compare);
                    return true;
                }
                Double valueBench = aggregatsBenchmark.readDouble(pointIdBench, measureIdBench);
                Double valueDiff = valueBench - value2compare;
                retriever.write(pointLocation, valueDiff);
                return true;
            }
        });
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}