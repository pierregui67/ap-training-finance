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

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=TimeShiftedPortfolioValuePostProcessor.PLUGIN_KEY)
public class TimeShiftedPortfolioValuePostProcessor extends ALocationShiftPostProcessor<Double> {

    public static final String PLUGIN_KEY="TIME_SHIFTED_PORTFOLIO_VALUE";

    public static int classic = 0;

    protected ILevelInfo levelToShift;

    public TimeShiftedPortfolioValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

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

        //The level is being asked for, check the location doesn't have null
        if(LocationUtil.getDepth(evaluationLocation, levelToShift.getHierarchyInfo()) != 0) {
            //If the location has null then we are prefetching and the location is returned
            Object day = LocationUtil.getCoordinate(evaluationLocation, levelToShift);
            if (day == null) {
                System.out.println("day == null");
                return evaluationLocation;
            }
            //If the day is 'Yesterday' then no aggregate should be retrieved, create an invalid location
            if (day == "Yesterday") {
                System.out.println("day == \"Yesterday\"");
                return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[]{ILevel.ALLMEMBER, "MISSING_MEMBER"});
            }
        }
        System.out.println("Classic");
        classic = classic + 1;
        System.out.println(classic);
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, "Yesterday"});
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        Double previousPV = (Double) underlyingMeasures[0];
        System.out.println(previousPV);
        return previousPV;
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}

