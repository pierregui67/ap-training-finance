package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = CumulativeReturnPostProcessor.PLUGIN_KEY)
public class CumulativeReturnPostProcessor extends AStream2PositionPostProcessor<Double> {

    public static final String PLUGIN_KEY = "CUMULATIVE_RETURN";

    public CumulativeReturnPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double getInitialPosition() {
        return null;
    }

    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
        return previousPosition == null ? (Double) currentValue : previousPosition + (Double) currentValue;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
