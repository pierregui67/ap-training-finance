package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = DynamicPortfolioValuePostProcessor.PLUGIN_KEY)
public class DynamicPortfolioValuePostProcessor extends ADynamicAggregationPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PV";

    private static final long serialVersionUID = 201410221517L;

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
        return (double) underlyingMeasures[0] * (double) underlyingMeasures[1];
    }

    @Override
    public void init(final Properties properties) throws QuartetException {
        super.init(properties);
        // addContextDependency(IReferenceCurrencyContext.class);
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }


}
