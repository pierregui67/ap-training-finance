package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = OriginalValuePostProcessor.PLUGIN_KEY)
public class OriginalValuePostProcessor extends AStream2PositionPostProcessor<Double>{

    public final static String PLUGIN_KEY = "ORIGINAL";

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public OriginalValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double getInitialPosition() {
        return 0.0;
    }

    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
        if (previousPosition == null || previousPosition == 0.0){
            return (Double) currentValue;
        }else
        return previousPosition;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}