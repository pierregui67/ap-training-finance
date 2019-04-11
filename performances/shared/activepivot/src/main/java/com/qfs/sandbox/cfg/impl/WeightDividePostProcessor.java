package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import javax.print.DocFlavor;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = WeightDividePostProcessor.PLUGIN_KEY)
public class WeightDividePostProcessor extends ABasicPostProcessor<Double> {

    final static public String PLUGIN_KEY = "DIVIDE_WEIGHT";
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public WeightDividePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        final double num = ((Number) underlyingMeasures[0]).doubleValue();
        final double num_percent = num * 100.0;
        final double total = ((Number) underlyingMeasures[1]).doubleValue();
        return num_percent /total;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
