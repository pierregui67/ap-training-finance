package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.IActivePivotVersion;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.IImpact;
import com.quartetfs.biz.pivot.query.aggregates.impl.AAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.impl.Impact;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import sun.plugin2.main.server.Plugin;

import java.util.Arrays;
import java.util.Set;


@QuartetExtendedPluginValue(intf = IAggregatesContinuousHandler.class, key = ForexHandler.PLUGIN_KEY)
public class ForexHandler extends AAggregatesContinuousHandler<Object> {

    public static final String PLUGIN_KEY = "FOREX_HANDLER";

    public static final String FOREX_LEVEL = "Currency";
    protected ILevelInfo level;

    private String currencyLevel;


    public ForexHandler(IActivePivotVersion pivotVersion) throws QuartetException{
        super(pivotVersion);
        final ILevel iLevel = HierarchiesUtil.getLevel(pivot, FOREX_LEVEL);
        if(iLevel == null){
            throw new QuartetException("Unable to find level" + FOREX_LEVEL);
        }

        level = iLevel.getLevelInfo();
    }

    public void setCurrencyLevel(String currencyLevel) {
        this.currencyLevel = currencyLevel;
    }

    @Override
    public IImpact computeImpact(ILocation iLocation, Object o) {
        Set<ILocation> impactedLocs = LocationUtil.expandAll(pivot.getHierarchies(), Arrays.asList(iLocation));

        // TODO : could be possible that a currency has been removed ?

        return new Impact(iLocation, impactedLocs, null);
    }

    @Override
    public String getStreamKey() {
        return ForexStream.PLUGIN_KEY;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
