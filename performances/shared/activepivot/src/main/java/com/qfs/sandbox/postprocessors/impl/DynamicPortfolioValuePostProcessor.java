package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

/**
 * calculate the value of the stock for each numberstock & close.avg
 */
@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = DynamicPortfolioValuePostProcessor.PLUGIN_KEY)
public class DynamicPortfolioValuePostProcessor extends ADynamicAggregationPostProcessor<Double> {


    public static final String PLUGIN_KEY = "DYNAMIC_PORTFOLIO_VALUE";

    private static final long serialVersionUID = 201804101621L;

    public DynamicPortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);
    }


    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        // understanding purpose
        //        System.out.println("info "+underlyingMeasures[0]+ " "+ underlyingMeasures[1]);
        return new Double((int) underlyingMeasures[0]) * (double) underlyingMeasures[1];
    }


}
