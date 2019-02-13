package com.qfs.sandbox.handlerstream;

import com.quartetfs.biz.pivot.IActivePivotVersion;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.IImpact;
import com.quartetfs.biz.pivot.query.aggregates.impl.AAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.impl.Impact;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Collections;
import java.util.Set;

@QuartetExtendedPluginValue(intf = IAggregatesContinuousHandler.class, key = ForexHandler.PLUGIN_KEY)
public class ForexHandler extends AAggregatesContinuousHandler<Object> {

    public static final String PLUGIN_KEY = "FOREX_HANDLER";

    public ForexHandler(IActivePivotVersion pivot) {
        super(pivot);
    }

    @Override
    public IImpact computeImpact(ILocation iLocation, Object o) {
        Set<ILocation> impactedSubLocation = LocationUtil.expandAll(pivot.getHierarchies(), Collections.singleton(iLocation));
        return new Impact(iLocation, impactedSubLocation, null);
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
