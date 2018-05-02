package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.ModifiedLocation;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ATimeLinePostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Collection;
import java.util.Properties;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=InitialValuePostProcessor.PLUGIN_KEY)
public class InitialValuePostProcessor extends ATimeLinePostProcessor<Object> {

    public static final String PLUGIN_KEY = "INITIAL_VALUE";

    /**
     * Constructor
     * @param name The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public InitialValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        positionType = PositionTypes.PREVIOUS_STREAM;
    }

    @Override
    protected Object getInitialPosition() {
        return null;
    }

    @Override
    protected Object aggregateNextEntry(Object previousPosition, Object currentValue) {
        if (previousPosition == null)
            return currentValue;
        return previousPosition;
    }


    @Override
    public Object[] computeRequiredLocations(ILocation location, Collection<Object[]> oRangeLocationArrays, Collection<ILocation> oLocations) {
        final int lvlDepth = location.getLevelDepth(timeHierarchyIdx);

        Object[] timeTemplate = LocationUtil.copyPath(location, timeHierarchyIdx, lvlDepth);

        if (oRangeLocationArrays != null)
            oRangeLocationArrays.add(new Object[lvlDepth]);
        if (oLocations != null)
            oLocations.add(new ModifiedLocation(location, timeHierarchyIdx, new Object[lvlDepth]));

        return timeTemplate;
    }



    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }

}
