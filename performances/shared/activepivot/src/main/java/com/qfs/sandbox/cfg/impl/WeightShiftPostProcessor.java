package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.List;
import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = WeightShiftPostProcessor.PLUGIN_KEY)
public class WeightShiftPostProcessor extends ALocationShiftPostProcessor<Double> {


    public static final String PLUGIN_KEY = "SHIFT_WEIGHT";
    public static final String LEVEL_PROPERTY = "LevelToShift";
    protected ILevelInfo levelToShift;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public WeightShiftPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }
    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(),properties.getProperty(LEVEL_PROPERTY));
        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getLevelInfo();
    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
        String test = ILevel.ALLMEMBER;
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(),new Object[] {ILevel.ALLMEMBER});
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
