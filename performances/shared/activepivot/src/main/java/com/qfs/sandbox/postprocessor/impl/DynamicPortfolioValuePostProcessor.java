package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = DynamicPortfolioValuePostProcessor.PLUGIN_KEY)
public class DynamicPortfolioValuePostProcessor extends ADynamicAggregationPostProcessor<Double, Double> {

    public static final String PLUGIN_KEY = "PORTFOLIO_VALUE";

    /**
     * Constructor
     *
     * @param name The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public DynamicPortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Double qty = (Double) underlyingMeasures[0];
        Double price = (Double) underlyingMeasures[1];
        Double pv = qty * price;
        return pv;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }


}
