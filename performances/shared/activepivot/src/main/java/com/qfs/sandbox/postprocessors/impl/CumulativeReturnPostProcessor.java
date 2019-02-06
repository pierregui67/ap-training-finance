package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = CumulativeReturnPostProcessor.PLUGIN_KEY)
public class CumulativeReturnPostProcessor extends AStream2PositionPostProcessor<Double> {

    public static final String PLUGIN_KEY = "CUMULATIVE_RETURN";

    private Double initValue = 0.0;
    private Double prevValue = 0.0;
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public CumulativeReturnPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double getInitialPosition() {
        return 0.0;
    }

    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
        Double res;
        if((double)currentValue == 0.0){
            currentValue = this.prevValue;
        }
        if(this.initValue == 0.0){
            this.initValue = (double)currentValue;
            this.prevValue = this.initValue;
        }

        res = (((double)currentValue - initValue) / this.initValue) * 100;
        this.prevValue = (double)currentValue;

        return res;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
