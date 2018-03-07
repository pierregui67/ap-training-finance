package com.qfs.sandbox.postprocessor.impl;

import com.qfs.logging.MessagesCore;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.PointLocationListReader;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.util.MessageUtil;

import java.util.Iterator;
import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_RATE;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE_NAME;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key =ForexDisplayPostProcessor.PLUGIN_KEY)
public class ForexDisplayPostProcessor extends ADynamicAggregationPostProcessor {

    public static final String PLUGIN_KEY = "FOREX_DISPLAY";

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public ForexDisplayPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Double rate;
        //leafLocation.getCoordinate();
        Iterator it = ((PointLocationListReader) leafLocation).getMapping().getHierarchies().iterator();
        int cpt = 0;
        IHierarchyInfo level;
        boolean found = false;
        while (it.hasNext() || !found){
            level = (IHierarchyInfo) it.next();
            // TODO : Should not be hard written.
            // Either create a global variable
            // Either add a key field in the PP which ask for the level to search.
            if (level.getName().equals("ForexHier")) {
                found = true;
            }
            else
                cpt ++;
        }
        if (!found)
            return null;
        // We must retrieve the first dimension which is Measure because it is not take into
        // count in the level count.
        String targetCurrency = (String) leafLocation.getCoordinate(cpt-1, 0);
        IRecordReader record = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE_NAME,
                new Object[] {"EUR", targetCurrency}, FOREX_RATE);
        if (record != null) {
            rate = (Double) record.read(FOREX_RATE);
            return rate;
        }
        return null;
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        /*if(evaluator == null) {
            throw new IllegalStateException(MessageUtil.formMessage(MessagesCore.BUNDLE,
                    MessagesCore.EXC_DEF_MISSING_EVALUATOR, getType(), EVALUATOR));
        }*/
    }

    /*@Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {

    }*/

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
