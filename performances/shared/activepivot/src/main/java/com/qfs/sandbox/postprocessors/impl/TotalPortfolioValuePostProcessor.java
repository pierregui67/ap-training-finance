package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=TotalPortfolioValuePostProcessor.PLUGIN_KEY)
public class TotalPortfolioValuePostProcessor extends ALocationShiftPostProcessor<Double> {

    public static final String PLUGIN_KEY="TOTAL_PORTFOLIO_VALUE";

    protected ILevelInfo levelToShift;

    public TotalPortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }


    // http://support.quartetfs.com/confluence/display/AP5/ALocationShiftPostProcessor
    // same as on website
    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(), properties.getProperty("levelToShift"));

        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getLevelInfo();

    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER});
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        return (double) underlyingMeasures[0];

    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}