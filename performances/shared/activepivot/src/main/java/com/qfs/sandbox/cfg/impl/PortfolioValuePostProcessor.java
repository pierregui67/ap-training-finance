package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import sun.rmi.rmic.iiop.Generator;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioValuePostProcessor.PLUGIN_KEY)
public class PortfolioValuePostProcessor extends ADynamicAggregationPostProcessor<Double> {
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */

    public static final String PLUGIN_KEY = "PORTFOLIOS_VALUES";
    public PortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Double stockValue = (Double)underlyingMeasures[0];
        Integer stockNumber = (Integer)underlyingMeasures[1];
        return stockValue*stockNumber;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
