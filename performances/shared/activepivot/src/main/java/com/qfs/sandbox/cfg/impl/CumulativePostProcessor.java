package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = CumulativePostProcessor.PLUGIN_KEY)
public class CumulativePostProcessor extends AStream2PositionPostProcessor<Double>{

    public static final String PLUGIN_KEY = "CUMULATIVE_EVOLUTION";
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public CumulativePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double getInitialPosition() {
        return 0.0;
    }

    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
        return  (Double) currentValue + previousPosition;

    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
