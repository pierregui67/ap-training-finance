package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = SoustractionPostProcessor.PLUGIN_KEY)
public class SoustractionPostProcessor extends ABasicPostProcessor<Double> {
    public final static String PLUGIN_KEY = "SoustractionPP";
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public SoustractionPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        final double d1;
        final double d2;
        if (underlyingMeasures[0] == null || underlyingMeasures[1] == null){
            return 0.0;
        }
        d1 = ((Number) underlyingMeasures[0]).doubleValue();
        d2 = ((Number) underlyingMeasures[1]).doubleValue();
        if (d1 == 0.0 || d2 == 0.0){
            return 0.0;
        }
        return d1 - d2;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
