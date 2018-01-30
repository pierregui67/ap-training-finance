package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = WeightPostProcessor.PLUGIN_VALUE)
public class WeightPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_VALUE = "WEIGHT";

    public WeightPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        double portfolioValue = (double) underlyingMeasures[0];
        double price = (double) underlyingMeasures[1];

        return portfolioValue == 0 ? null : price / portfolioValue;
    }

    @Override
    public String getType() {
        return PLUGIN_VALUE;
    }
}
