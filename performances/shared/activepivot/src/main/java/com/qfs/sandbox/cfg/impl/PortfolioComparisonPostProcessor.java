package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IPointLocation;
import com.quartetfs.biz.pivot.IPointLocationReader;
import com.quartetfs.biz.pivot.cellset.IAggregatesRetrievalResult;
import com.quartetfs.biz.pivot.cellset.IPointProcedure;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AAdvancedPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.UnderlyingMeasuresPrefetcher;
import com.quartetfs.biz.pivot.query.aggregates.IAdvancedAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;
import java.util.stream.Collectors;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioComparisonPostProcessor.PLUGIN_KEY)
public class PortfolioComparisonPostProcessor extends AAdvancedPostProcessor<Double> {
    public static final String PLUGIN_KEY = "COMPARISON";
    protected IHierarchy portfoliosHierarchy;

    /**
     * Constructor.
     *
     * @param name            The name of the post-processor instance.
     * @param creationContext The creation context that contains all additional parameters for the post-processor construction.
     */
    public PortfolioComparisonPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException{
        super.init(properties);
        portfoliosHierarchy = getActivePivot().getHierarchies().stream().filter(o -> o.getName().contains("Portfolios")).collect(Collectors.toList()).get(0);
        prefetchers.add(new CustomPrefetcher(this.getMeasuresProvider(), portfoliosHierarchy, underlyingMeasures));
    }

    @Override
    public void compute(ILocation iLocation, IAdvancedAggregatesRetriever iAdvancedAggregatesRetriever) throws QuartetException {

        ILocation aggregateLocation = LocationUtil.createModifiedLocation(iLocation, portfoliosHierarchy.getHierarchyInfo(),  new Object[] {ILevel.ALLMEMBER, null});

        IAggregatesRetrievalResult aggregats = iAdvancedAggregatesRetriever.retrieveAggregates(aggregateLocation, underlyingMeasures);

        int measureId = aggregats.getMeasureId(underlyingMeasures[0]);
        aggregats.forEachPoint(new IPointProcedure() {
            @Override
            public boolean execute(IPointLocationReader iPointLocationReader, int pointId) {
                ILocation shift = LocationUtil.createModifiedLocation(iPointLocationReader, portfoliosHierarchy.getHierarchyInfo(),  new Object[] {ILevel.ALLMEMBER, "benchmark"});
                Double delta;
                try{
                    int shiftID = aggregats.getRow(shift);
                    Double shiftValue = aggregats.readDouble(shiftID, measureId);
                    Double value = aggregats.readDouble(pointId, measureId);
                    delta = value - shiftValue;}
                catch(Exception e) {
                    delta = null;}
                //no filter case
                if (iLocation.getCoordinate(1,1) == null) {
                    if(!(iPointLocationReader.getCoordinate(1, 1).toString().equalsIgnoreCase("benchmark"))) {
                        iAdvancedAggregatesRetriever.write(iPointLocationReader, delta);
                    } }
                //filter case
                else if(iPointLocationReader.getCoordinate(1, 1)== iLocation.getCoordinate(1,1)) {
                    iAdvancedAggregatesRetriever.write(iPointLocationReader, delta);
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
