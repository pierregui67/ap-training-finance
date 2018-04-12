package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;


/**
 * Class which calculates the value of a portfolio
 * warning : it only takes the numberofstock & close.avg for one date
 */
@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PortfolioValuePostProcessor.PLUGIN_KEY)
public class PortfolioValuePostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PORTFOLIO_VALUE";

    private static final long serialVersionUID = 201804091741L;

    public PortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        return new Double((int) underlyingMeasures[0]) * (double)underlyingMeasures[1];

    }
}
