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

@QuartetExtendedPluginValue(intf= IPostProcessor.class, key=ShiftedPortfolioValuePostProcessor.PLUGIN_KEY)
public class ShiftedPortfolioValuePostProcessor extends ALocationShiftPostProcessor<Double>{

    public static final String PLUGIN_KEY="SHIFTED_PORTFOLIO_VALUE";

    protected ILevelInfo levelToShift;

    public ShiftedPortfolioValuePostProcessor (String name, IPostProcessorCreationContext creationContext) {
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
            if(day == null)
                return evaluationLocation;
            //If the day is 'Yesterday' then no aggregate should be retrieved, create an invalid location
            if(day == "Yesterday")
                return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, "MISSING_MEMBER"});
        }

        //Return the Location asking for Yesterday
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, "Yesterday"});
    }
    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }

}
