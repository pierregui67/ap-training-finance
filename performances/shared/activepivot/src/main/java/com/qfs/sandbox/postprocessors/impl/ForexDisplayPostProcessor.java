package com.qfs.sandbox.postprocessors.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexDisplayPostProcessor.PLUGIN_KEY)
public class ForexDisplayPostProcessor extends ForexPostProcessor {



    public static final String PLUGIN_KEY = "FOREX_DISPLAY";

    public ForexDisplayPostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {

        IHierarchy hier = null;
        try {
            hier = HierarchiesUtil.getHierarchy(getActivePivot(), properties.getProperty("DimensionName"));
        } catch (QuartetException e) {
            e.printStackTrace();
        }

        int ordinal = hier.getOrdinal();

        String targetCurrency = (String) LocationUtil.copyPath(location, ordinal-1)[0];

        Double ratio = getRatioTwoCurrency("EUR", targetCurrency);
        return (double) underlyingMeasures[0] * ratio;
    }
}
