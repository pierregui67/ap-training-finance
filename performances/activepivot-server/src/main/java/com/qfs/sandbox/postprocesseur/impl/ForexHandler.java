package com.qfs.sandbox.postprocesseur.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;
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

import java.util.*;

@QuartetExtendedPluginValue(intf=IAggregatesContinuousHandler.class, key = ForexHandler.PLUGIN_KEY)
public class ForexHandler extends AAggregatesContinuousHandler<Object> {

    public static final String PLUGIN_KEY = "FOREX_HANDLER";

    public static final String FOREX_LEVEL = "Currency";
    protected ILevelInfo level;

    private String currencyLevel;

    /**
     * Setter to allow injection of currency level in Spring XML file
     * @param currencyLevel the name of the currency level
     */
    public void setCurrencyLevel(String currencyLevel) {
        this.currencyLevel = currencyLevel;
    }

    public ForexHandler(IActivePivotVersion pivot) throws QuartetException{
        super(pivot);
        final ILevel iLevel = HierarchiesUtil.getLevel(pivot, FOREX_LEVEL);
        if(iLevel == null) {
            throw new QuartetException("Unable to find level :" + FOREX_LEVEL);
        }
        level = iLevel.getLevelInfo();
    }

    @Override
    public IImpact computeImpact(ILocation location, Object event) {

        Set<ILocation> impactedLocs;

        if (!(event instanceof Set))
            return new Impact(location, null, null);
        Set<String> updatedCurrencies = (Set<String>) event;


        if (updatedCurrencies.contains(pivot.getContext().get(ICurrencyContextValue.class).getCurrency()))
            impactedLocs = LocationUtil.expandAll(pivot.getHierarchies(), Arrays.asList(location));
        else
            impactedLocs = null;

        // TODO : could it be possible that a currency has been removed ?
        return new Impact(location, impactedLocs, null);
    }

    @Override
    public String getStreamKey() {
        return ForexStream.PLUGIN_KEY;
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}
