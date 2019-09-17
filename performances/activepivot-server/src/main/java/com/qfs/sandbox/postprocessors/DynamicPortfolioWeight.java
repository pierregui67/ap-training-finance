package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf= IPostProcessor.class, key = DynamicPortfolioWeight.PLUGIN_KEY)
public class DynamicPortfolioWeight extends ADynamicAggregationPostProcessor {

    public final static String PLUGIN_KEY = "PORTFOLIOWEIGHT";

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public DynamicPortfolioWeight(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }


    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        if((Double)underlyingMeasures[1] == 0) {
            return null;
        }
        return (Double)underlyingMeasures[0]/(Double)underlyingMeasures[1];
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
