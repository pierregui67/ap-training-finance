package com.qfs.sandbox.postprocessor.impl;

import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_RATE;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE_NAME;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key =ForexDisplayPostProcessor.PLUGIN_KEY)
public class ForexDisplayPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "DISPLAY";

    public static final String HIER_FOREX = "hierForex";
    protected String hierForex="ForexHier";

    protected int hierarchyOrdinal;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this
     *                        post-processor.
     */
    public ForexDisplayPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        if (properties.containsKey(HIER_FOREX)) {
            this.hierForex = properties.getProperty(HIER_FOREX);
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is " +
                    "missing the mandatory property " + HIER_FOREX);
        }

        // Getting the ordinal of the wished hierarchy.
        final IHierarchy hier = HierarchiesUtil.getHierarchy(getActivePivot(), hierForex);
        if (hier == null) {
            throw new PostProcessorInitializationException("Unable to find hierarchy " + hierForex
                    + " in cube "
                    + getActivePivot().getId());
        }
        hierarchyOrdinal = hier.getOrdinal();
        if (hierarchyOrdinal < 1)
            throw new PostProcessorInitializationException("Impossible to find hierarchy: " + hierForex);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {

        // Forbidding other dimensions in the view.
        for (int i=0; i<location.getHierarchyCount(); i++) {
            if (location.getLevelDepth(i) != 1 && i != hierarchyOrdinal-1)
                throw new IllegalArgumentException("To display the ForexRate the only tolerate hierarchy" +
                        " in the view is the Forex Hierarchy");
        }
        if (location.getLevelDepth(hierarchyOrdinal-1) == 1)
            throw new IllegalArgumentException("To display the ForexRate the Forex Hierarchy must be in the view. " +
                    "Note that including the Forex Dimension or the Forex Hierarchy is not correct.");

        Double rate;
        String targetCurrency = (String) location.getCoordinate(hierarchyOrdinal-1, 1);
        IRecordReader record = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE_NAME,
                new Object[] {"EUR", targetCurrency}, FOREX_RATE);
        if (record != null) {
            rate = (Double) record.read(FOREX_RATE);
            return rate;
        }
        throw new IllegalArgumentException("The query to the forex store does not return valid result. " +
                "Check the currencies validity.");
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
