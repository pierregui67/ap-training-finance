package com.qfs.sandbox.postprocessor.impl;

import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Date;
import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.RATE;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexDisplayPostProcessor.PLUGIN_KEY)
public class ForexDisplayPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "FOREX_RATE";

    private ILevelInfo forexLevelInfo = null;
    private ILevelInfo dateLevelInfo = null;

    public ForexDisplayPostProcessor(String name, IPostProcessorCreationContext creationContext) throws QuartetException {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        String forexLevelDescription = properties.getProperty("level");
        ILevel forexLevel = HierarchiesUtil.getLevel(getActivePivot(), forexLevelDescription);
        if (forexLevel == null) {
            throw new QuartetRuntimeException("Unable to find forex level with description " + forexLevelDescription);
        }
        forexLevelInfo = forexLevel.getLevelInfo();

        String dateLevelDescription = properties.getProperty("dateLevel");
        ILevel dateLevel = HierarchiesUtil.getLevel(getActivePivot(), dateLevelDescription);
        if (dateLevel == null) {
            throw new QuartetRuntimeException("Unable to find date level with description " + dateLevelDescription);
        }
        dateLevelInfo = dateLevel.getLevelInfo();
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {

        if (!LocationUtil.isAtLevel(location,dateLevelInfo) || !LocationUtil.isAtLevel(location, forexLevelInfo)) {
            return null;
        }
        String currencyPair = (String) LocationUtil.getCoordinate(location, forexLevelInfo);
        //one should not aggregate across conversion rate
        if (currencyPair.equals(ILevel.ALLMEMBER)) {
            return null;
        }
        Date date = (Date) LocationUtil.getCoordinate(location, dateLevelInfo);
        final IRecordReader r = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE, new Object[] {currencyPair, date}, RATE);
        return (r == null) ? null : r.readDouble(0);
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
