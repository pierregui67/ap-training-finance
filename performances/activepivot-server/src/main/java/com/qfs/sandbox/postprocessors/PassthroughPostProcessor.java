package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IPointLocationReader;
import com.quartetfs.biz.pivot.cellset.IAggregatesRetrievalResult;
import com.quartetfs.biz.pivot.cellset.IPointProcedure;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.ModifiedLocation;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.AAdvancedPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.APrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.UnderlyingMeasuresPrefetcher;
import com.quartetfs.biz.pivot.query.aggregates.IAdvancedAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.*;
import java.util.function.BiFunction;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PassthroughPostProcessor.PLUGIN_KEY)
public class PassthroughPostProcessor extends AAdvancedPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PASSTHROUGH";

    public PassthroughPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        prefetchers.add(new UnderlyingMeasuresPrefetcher(this.getMeasuresProvider(), underlyingMeasures));
    }

    @Override
    public void compute(ILocation location, IAdvancedAggregatesRetriever retriever) {
        // Récupère les aggrégats de la location demandée
        IAggregatesRetrievalResult aggregats = retriever.retrieveAggregates(location, underlyingMeasures);

        // Itère sur les aggrégats et écrit le résultat sur chaque point location
        int measureId = aggregats.getMeasureId(underlyingMeasures[0]);

        aggregats.forEachPoint(new IPointProcedure() {
            @Override
            public boolean execute(IPointLocationReader pointLocation, int pointId) {
                Double value = aggregats.readDouble(pointId, measureId);
                retriever.write(pointLocation, value);
                return true;
            }
        });
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
