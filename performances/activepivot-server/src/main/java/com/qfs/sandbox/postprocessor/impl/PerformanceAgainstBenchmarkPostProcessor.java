package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IPointLocationReader;
import com.quartetfs.biz.pivot.cellset.IAggregatesRetrievalResult;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cellset.IPointProcedure;
import com.quartetfs.biz.pivot.cellset.IValueProcedure;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.ModifiedLocation;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.AAdvancedPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.APrefetcher;
import com.quartetfs.biz.pivot.query.aggregates.IAdvancedAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.*;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PerformanceAgainstBenchmarkPostProcessor.PLUGIN_KEY)
public class PerformanceAgainstBenchmarkPostProcessor extends AAdvancedPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PERFORMANCE";

    private IHierarchyInfo portfolioHierarchyInfo = null;

    public PerformanceAgainstBenchmarkPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        String hierarchy = properties.getProperty("portfolioHierarchy");
        IHierarchy h = HierarchiesUtil.getHierarchy(getActivePivot(), hierarchy);
        if (h == null) {
            throw new QuartetException("Unable to find hierarchy for property : portfolioHierarchy" + hierarchy);
        }
        portfolioHierarchyInfo = h.getHierarchyInfo();

        //prefetch values of portfolio for current P and benchmark B
        IPrefetcher performancePrefetcher = new APrefetcher(measuresProvider) {
            @Override
            protected Collection<ILocation> computeLocations(Collection<ILocation> locations) {
                List<ILocation> locationsList = new ArrayList<>();
                for (ILocation loc : locations) {
                    locationsList.add(loc);
                        ILocation benchmarkLoc = new ModifiedLocation(loc, h.getOrdinal()-1, new Object[] {ILevel.ALLMEMBER,"benchmark"});
                        locationsList.add(benchmarkLoc);
                    }
                return locationsList;
            }

            @Override
            protected Collection<String> computeMeasures(Collection<ILocation> locations) {
                return Arrays.asList(underlyingMeasures);
            }
        };
        prefetchers.add(performancePrefetcher);

    }

    @Override
    public void compute(ILocation location, IAdvancedAggregatesRetriever retriever) throws QuartetException {
        // show difference in perf in percentage =/- xx%
        ILocation benchmarkLocation = new ModifiedLocation(location, portfolioHierarchyInfo.getOrdinal()-1, new Object[] {ILevel.ALLMEMBER,"benchmark"});
        IAggregatesRetrievalResult b = retriever.retrieveAggregates(benchmarkLocation, underlyingMeasures);
        IAggregatesRetrievalResult r = retriever.retrieveAggregates(location, underlyingMeasures);

        //get index of the udl measure in the result
        int rMeasureId = r.getMeasureId(underlyingMeasures[0]);
        int bMeasureId = b.getMeasureId(underlyingMeasures[0]);

        //get benchmark value for each date in location scope
        Map<Date,Double> benchmarkDateMap = new HashMap<>();
        b.forEachPoint(new IPointProcedure() {
            @Override
            public boolean execute(IPointLocationReader bReader, int bPointId) {
                Date bDate = (Date) b.getCoordinate(bPointId,0,0);
                Double benchmarkValue = (Double) b.read(bPointId, bMeasureId);
                benchmarkDateMap.put(bDate,benchmarkValue);
                return true;
            }
        });

        //get value of portfolio for each point and compute difference with benchmark before writing result
        r.forEachPoint(new IPointProcedure() {
            @Override
            public boolean execute(IPointLocationReader reader, int pointId) {
                int outgoingPoint = retriever.addPoint(reader);
                Double portfolioValue = (Double) r.read(pointId, rMeasureId);
                Date date = (Date) r.getCoordinate(pointId,0,0);
                Double benchmark = benchmarkDateMap.get(date);
                if (portfolioValue == null) {
                    retriever.write(outgoingPoint, benchmark);
                } else if (benchmark == null) {
                    retriever.write(outgoingPoint, portfolioValue);
                } else {
                    Double diff = benchmark - portfolioValue;
                    retriever.write(outgoingPoint, diff);
                }
                return true;
            }
        });
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
