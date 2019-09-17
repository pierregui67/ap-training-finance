package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ALocationShiftPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf= IPostProcessor.class, key = DynamicShiftToPortfolio.PLUGIN_KEY)
public class DynamicShiftToPortfolio extends ALocationShiftPostProcessor<Double> {

    public final static String PLUGIN_KEY = "ShiftToPortfolio";
    protected IHierarchyInfo levelToShift;
    protected String portfolioToShow;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */

    public DynamicShiftToPortfolio(String name, IPostProcessorCreationContext creationContext) throws QuartetException {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(), properties.getProperty("levelToShift"));
        portfolioToShow = properties.getProperty("portfolioToShow");
        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getHierarchyInfo();
    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
        return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift, new Object[] {ILevel.ALLMEMBER, portfolioToShow});
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
