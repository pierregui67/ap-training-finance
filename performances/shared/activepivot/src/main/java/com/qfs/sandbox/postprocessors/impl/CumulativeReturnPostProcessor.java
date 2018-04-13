package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = CumulativeReturnPostProcessor.PLUGIN_KEY)
public class CumulativeReturnPostProcessor extends AStream2PositionPostProcessor<Double> {

    public static final String PLUGIN_KEY = "CUMULATIVE_RETURN";

    private static final long serialVersionUID = 201804121544L;

    private Double initialValue = 0.0;
    private Double prevValue = 0.0;

    public CumulativeReturnPostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);
    }

    @Override
    protected Double getInitialPosition() {
        return 0.0;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
//        return previousPosition + (double)currentValue
        Double res;


        if ((double)currentValue == 0.0){
            currentValue = this.prevValue;
        }

        if (this.initialValue == 0.0){
            this.initialValue = (double)currentValue;
            this.prevValue = this.initialValue;
        }

        res = (((double)currentValue - initialValue )/this.initialValue)*100;
        this.prevValue = (double)currentValue;

        return res;

    }
}
