package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.ModifiedLocation;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ATimeLinePostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Collection;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=InitialValuePostProcessor.PLUGIN_KEY)
public class InitialValuePostProcessor extends ATimeLinePostProcessor<Object> {

    public static final String PLUGIN_KEY = "INITIAL_VALUE";

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public InitialValuePostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);

        positionType = PositionTypes.PREVIOUS_STREAM;

        // Init prefetcher
        this.prefetchers.add(new TimeLinePrefetcher(this));
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
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
    public Object[] computeRequiredLocations(ILocation location, Collection<Object[]> rangeLocationArrays, Collection<ILocation> locations) {
        final int lvlDepth = location.getLevelDepth(timeHierarchyIdx);

        Object[] timeTemplate = LocationUtil.copyPath(location, timeHierarchyIdx, lvlDepth);

        if (rangeLocationArrays != null)
            rangeLocationArrays.add(new Object[lvlDepth]);
        if (locations != null)
            locations.add(new ModifiedLocation(location, timeHierarchyIdx, new Object[lvlDepth]));

        return timeTemplate;
    }
}
