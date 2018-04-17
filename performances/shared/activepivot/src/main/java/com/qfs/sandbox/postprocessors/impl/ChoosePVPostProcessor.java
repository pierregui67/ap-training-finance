package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ChoosePVPostProcessor.PLUGIN_KEY)
public class ChoosePVPostProcessor extends ALocationShiftPostProcessor<Double> {

    public static final String PLUGIN_KEY = "PORTFOLIO_CHOOSE";

    protected ILevelInfo levelToShift;

    protected String portfolioType;

    protected int hierarchyOrdinal;



    public ChoosePVPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;

    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(), properties.getProperty("levelToShift"));

        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getLevelInfo();

        portfolioType = properties.getProperty("portfolioType");

        final IHierarchy hierarchy = HierarchiesUtil.getHierarchy(getActivePivot(), properties.getProperty("hierarchyName"));

        hierarchyOrdinal = hierarchy.getOrdinal();
        if(hierarchyOrdinal < -1 ){
            throw new PostProcessorInitializationException("No hierarchy specified");
        }

    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, portfolioType});
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        if(location.getLevelDepth(hierarchyOrdinal - 1) == 1){ // when the depth of the selected hierachy is 1 return the value
            return (double) underlyingMeasures[0];
        }
        return null;
    }
}
