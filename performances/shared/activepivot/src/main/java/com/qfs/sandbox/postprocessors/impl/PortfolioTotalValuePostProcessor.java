package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;


@QuartetExtendedPluginValue(intf= IPostProcessor.class, key=PortfolioTotalValuePostProcessor.PLUGIN_KEY)
public class PortfolioTotalValuePostProcessor extends ALocationShiftPostProcessor<Double> {

    public static final String PLUGIN_KEY="PORTFOLIO_TOTAL_VALUE";

    protected ILevelInfo levelToShift;
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public PortfolioTotalValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[]{ILevel.ALLMEMBER});
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
