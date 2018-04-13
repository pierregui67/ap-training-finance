package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.AStream2PositionPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ATimeLinePostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Collection;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PeriodReturnPostProcessor.PLUGIN_KEY)
public class PeriodReturnPostProcessor extends AStream2PositionPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PERIOD_RETURN";

    private static final long serialVersionUID = 201804122915L;
    private Double prevValue = 0.0;

    public PeriodReturnPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    protected Double getInitialPosition() {
        return 0.0;
    }



    @Override
    protected Double aggregateNextEntry(Double previousPosition, Object currentValue) {
        Double res;
        if (prevValue == 0.0 && (double)currentValue != 0.0){
            prevValue = (double)currentValue;
        }

        if( (double)currentValue != 0){
            res = (((double)currentValue - prevValue )/ this.prevValue)*100;
            prevValue = (double)currentValue;
        }else {
            return 0.0;
        }

        return res;
    }
}
