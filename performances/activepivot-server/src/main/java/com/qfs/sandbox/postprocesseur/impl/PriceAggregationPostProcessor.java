package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = PriceAggregationPostProcessor.PLUGIN_KEY)
public class PriceAggregationPostProcessor extends ADynamicAggregationPostProcessor {

    public static final String PLUGIN_KEY = "PRICE";

    public static final String LEAF_LEVELS = "leafLevels";

    protected ILevelInfo levelInfo;
    protected IHierarchy hier;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public PriceAggregationPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        if (properties.containsKey(LEAF_LEVELS)) {
            levelInfo = (ILevelInfo) this.leafLevelsInfo.get(0);
            hier = HierarchiesUtil.getHierarchy(getActivePivot(), "StockSymbol", "StockSymbol");
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + LEAF_LEVELS);
        }
    }

    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        return underlyingMeasures[0];
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}
