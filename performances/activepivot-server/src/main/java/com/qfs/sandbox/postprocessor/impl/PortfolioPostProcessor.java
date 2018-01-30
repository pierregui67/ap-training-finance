package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioPostProcessor.PLUGIN_KEY)
public class PortfolioPostProcessor extends ADynamicAggregationPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PORTFOLIOS";
    public PortfolioPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        int quantity = (int) underlyingMeasures[0];
        double stockPrice = (double) underlyingMeasures[1];
        return quantity * stockPrice;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
