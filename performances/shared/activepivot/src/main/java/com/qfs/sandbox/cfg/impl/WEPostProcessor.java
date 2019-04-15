package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = WEPostProcessor.PLUGIN_KEY)
public class WEPostProcessor extends ABasicPostProcessor<Double> {
    public static final String PLUGIN_KEY = "WE_NULL";
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public WEPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        final double value = ((Number) underlyingMeasures[0]).doubleValue();
        if (value == 0){ return null;}
        else return value;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
