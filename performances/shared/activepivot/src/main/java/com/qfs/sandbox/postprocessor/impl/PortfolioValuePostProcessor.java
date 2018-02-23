package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioValuePostProcessor.PLUGIN_KEY)
public class PortfolioValuePostProcessor extends ABasicPostProcessor<Double> {


    /**
     * Plugin key for access to this post-processor
     */
    public static final String PLUGIN_KEY = "PORTFOLIO_VALUE";

    /**
     * Constructor
     *
     * @param name The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public PortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
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



    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        return (double) underlyingMeasures[0] * (double) underlyingMeasures[1];
    }


}
