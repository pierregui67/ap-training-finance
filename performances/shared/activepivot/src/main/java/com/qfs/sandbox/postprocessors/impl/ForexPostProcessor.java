package com.qfs.sandbox.postprocessors.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;
import com.qfs.store.Types;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ADynamicAggregationPostProcessor<Double> {

    public static final String PLUGIN_KEY = "FOREX";

    public static final String REFERENCE_CURRENCY = "currency";

    protected ILevelInfo currencyLevelInfo = null;

    protected ICurrencyContextValue referenceCurrency;


    public ForexPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        String dateDescription = properties.getProperty("dateLevel");
        ILevel dateLevel = HierarchiesUtil.getLevel(getActivePivot(), dateDescription);
        if (dateLevel == null) {
            throw new QuartetRuntimeException("unable to find level for description :" +  dateDescription);
        }
        currencyLevelInfo = dateLevel.getLevelInfo();

        addContextDependency(ICurrencyContextValue.class);
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        return null;
    }

    @Override
    protected int getDataType() {
        return Types.TYPE_DOUBLE;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
