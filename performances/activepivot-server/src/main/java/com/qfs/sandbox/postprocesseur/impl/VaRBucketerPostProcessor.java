package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.ModifiedLocation;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ATimeLinePostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key= VaRBucketerPostProcessor.PLUGIN_KEY)
public class VaRBucketerPostProcessor extends ATimeLinePostProcessor<ArrayList<Double>> {

    public static final String PLUGIN_KEY = "VAR_BUCKETER";
    private static final int DAY_IN_VAR = 300;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public VaRBucketerPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        positionType = PositionTypes.PREVIOUS_STREAM;
    }

    @Override
    protected ArrayList<Double> aggregateNextEntry(ArrayList<Double> previousPosition, Object currentValue) {
        ArrayList<Double> list = new ArrayList<>();
        if (previousPosition == null) {
            ArrayList<Double> vector = new ArrayList<>();
            return vector;
        }
        if (currentValue == null)
            return null;
        Iterator<Double> it = previousPosition.iterator();
        if (previousPosition.size() == DAY_IN_VAR)
            it.next();
        while(it.hasNext())
            list.add(it.next());
        list.add((Double) currentValue);
        return list;
    }

    @Override
    protected ArrayList<Double> getInitialPosition() {
        return null;
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

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
