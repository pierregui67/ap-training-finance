package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.*;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=ChoosePVTypePostProcessor.PLUGIN_KEY)
public class ChoosePVTypePostProcessor<Double> extends ALocationShiftPostProcessor<Double> {

    public final static String PLUGIN_KEY = "CHOOSE_PV_TYPE";

    /**
     * One prohibits the presence of the portfolios type hierarchies in the view.
     * Indeed, it does not make any sense to show the difference of the portfolios performances
     * regarding to the type.
     */
    public static final String HIERARCHY_NAME = "hierarchyNameToForbid";

    public final static String PORTFOLIO_TYPE = "portfolioType";
    protected String portfolioType;

    protected ILevelInfo levelToShift;

    /** The ordinal of the hierarchy which should not be in the view */
    protected int hierarchyOrdinal;

    /**
     * Constructor.
     *
     * @param name            The name of the post-processor instance.
     * @param creationContext The creation context that contains all additional parameters for the post-processor construction.
     */
    public ChoosePVTypePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        // Init prefetcher
        //this.prefetchers.add(new ChoosePVTypePostProcessor.ChoosePVTypePrefetcher(this));
        super.init(properties);

        /* Init stream measure name */
        if (properties.containsKey(PORTFOLIO_TYPE)) {
            this.portfolioType = properties.getProperty(PORTFOLIO_TYPE);
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + PORTFOLIO_TYPE);
        }

        final ILevel level = HierarchiesUtil.getLevel(getActivePivot(), properties.getProperty("levelToShift"));

        if(level == null) {
            throw new QuartetException("Unable to find level for property levelToShift: " + properties.getProperty("levelToShift"));
        }
        levelToShift = level.getLevelInfo();

        // The code to forbid the Type hierarchy to be in the view
        // Retrieve the hierarchy
        final String hierarchyName = properties.getProperty(HIERARCHY_NAME);
        if (null == hierarchyName) {
            throw new PostProcessorInitializationException("No hierarchy specified. You must specify a hierarchy name using the property: " + HIERARCHY_NAME);
        }

        final IHierarchy hier = HierarchiesUtil.getHierarchy(getActivePivot(), hierarchyName);
        hierarchyOrdinal = hier.getOrdinal();
        if (hierarchyOrdinal < 1)
            throw new PostProcessorInitializationException("Impossible to find hierarchy: " + hierarchyName);
    }

    @Override
    public ILocation shiftLocation(ILocation evaluationLocation) {
            return LocationUtil.createModifiedLocation(evaluationLocation, levelToShift.getHierarchyInfo(), new Object[] {ILevel.ALLMEMBER, portfolioType});
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        if (location.getLevelDepth(hierarchyOrdinal - 1) == 1) {
            Double choosenPVType = (Double) underlyingMeasures[0];
            return choosenPVType;
        }
        return null;
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }

}

