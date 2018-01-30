package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ShiftTotalValuePostProcessor.PLUGIN_KEY)
public class ShiftTotalValuePostProcessor extends ALocationShiftPostProcessor {

    public static final String PLUGIN_KEY = "SHIFT_VALUE";

    protected List<IHierarchyInfo> hierarchiesInfo = new ArrayList<>();

    public ShiftTotalValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        String[] hierarchiesProp = properties.getProperty("hierarchyToShift").split(",");

        for (String h : hierarchiesProp) {
            IHierarchy hierarchy = HierarchiesUtil.getHierarchy(getActivePivot(), h);

            if(hierarchy == null) {
                throw new QuartetException("Unable to find hierarchy for property levelToShift: " + h);
            }

            IHierarchyInfo hierarchyInfo = hierarchy.getHierarchyInfo();
            hierarchiesInfo.add(hierarchyInfo);
        }


    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {


//        if (LocationUtil.getDepth(evaluationLocation, hierarchyInfo) == 0) {        }
        /*
        //The level is being asked for, check the location doesn't have null
        if(LocationUtil.hasDepth(evaluationLocation, levelToShift)) {
            //If the location has null then we are prefetching and the location is returned
            Object day = LocationUtil.getCoordinate(evaluationLocation, levelToShift);
            if(day == null)
                return evaluationLocation;
            //If the day is 'Yesterday' then no aggregate should be retrieved, create an invalid location
            if(day == "Yesterday")
                return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, "MISSING_MEMBER"});
        }
*/
        //Return the Location asking for Yesterday
        return LocationUtil.createModifiedLocation(evaluationLocation, hierarchiesInfo, new Object[][] {{ILevel.ALLMEMBER},{ILevel.ALLMEMBER}});
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
