package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ATimeLinePostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.PreviousValuePostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Collection;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PeriodReturnPostProcessor.PLUGIN_KEY)
public class PeriodReturnPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PERIOD_RETURN";

    public PeriodReturnPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        Double previous = (Double) underlyingMeasures[0];
        Double current = (Double) underlyingMeasures[1];
        return previous == null ? current :  current == null ? - previous : current - previous;
    }



    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
