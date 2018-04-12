package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioWeightPostProcessor.PLUGIN_KEY)
public class PortfolioWeightPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PORTFOLIO_WEIGHT";


    public PortfolioWeightPostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);

    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        if ((double)underlyingMeasures[1] != 0.0){
            return (double)underlyingMeasures[0] / (double)underlyingMeasures[1];
        }
        else {
            return 0.0;
        }
    }
}
