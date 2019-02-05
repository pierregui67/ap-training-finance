package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

/**
 * PortfolioValuePostProcessor (Calculate the value of a portfolio)
 */
@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioValuePostProcessor.PLUGIN_KEY)
public class PortfolioValuePostProcessor extends ADynamicAggregationPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PORTFOLIO_VALUE";

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public PortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluateLeaf(ILocation location, Object[] underlyingMeasures) {
        return (double) (int) underlyingMeasures[0] * (double)underlyingMeasures[1];
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
