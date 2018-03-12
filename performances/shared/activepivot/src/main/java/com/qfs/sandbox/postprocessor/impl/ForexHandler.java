package com.qfs.sandbox.postprocessor.impl;

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

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

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

        /*//get the updated currencies from the Forex Stream
        if (!(event instanceof Set))
            return new Impact(location, null, null);
        Set<String> updatedCurrencies = (Set<String>) event; // != null

        //compute the impacted locations
        Collection<ILocation> impactedLocs = new ArrayList<ILocation>();
        // When we display the store, all the cell of the impacted currency must be updated.
        for (String currency : updatedCurrencies) {
            ILocation loc = LocationUtil.createModifiedLocation(location, level.getHierarchyInfo(), new Object[] {currency});
            impactedLocs.add(loc);
        }*/
        // TODO : what should we use ?
        Set<ILocation> impactedLocs = LocationUtil.expandAll(pivot.getHierarchies(),
                Arrays.asList(location));

        // TODO : could be possible that a currency has been removed ?

        return new Impact(location, (Set<ILocation>) impactedLocs, null);
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
