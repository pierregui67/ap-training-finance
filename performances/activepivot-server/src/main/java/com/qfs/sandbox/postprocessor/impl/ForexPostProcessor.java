package com.qfs.sandbox.postprocessor.impl;

import com.qfs.sandbox.contextvalue.ACurrencyContextValue;
import com.qfs.sandbox.contextvalue.impl.Currencies;
import com.qfs.sandbox.contextvalue.impl.CurrencyContextValue;
import com.qfs.store.IDatastoreVersion;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.biz.pivot.query.IQueryCache;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.impl.Pair;

import javax.xml.stream.Location;
import java.util.Date;
import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.RATE;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ADynamicAggregationPostProcessor<Double> {

    public static final String PLUGIN_KEY = "FOREX";
    private ILevelInfo dateLevelInfo = null;


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
        dateLevelInfo = dateLevel.getLevelInfo();

        addContextDependency(ACurrencyContextValue.class);
    }

    String getCurrency() {
        ACurrencyContextValue currencyContext = getActivePivot().getContext().get(ACurrencyContextValue.class);
        if (currencyContext == null) {
            throw new QuartetRuntimeException("Cannot retrieve the currency context from post-processor " + getType());
        }
        return currencyContext.getCurrency();
    }

    private Double getRate(IDatastoreVersion dv, Pair forexKeys) {
        final IRecordReader r = DatastoreQueryHelper.getByKey(dv, FOREX_STORE, new Object[] {forexKeys.getLeft(),forexKeys.getRight()}, RATE);
        return (r == null) ? null : r.readDouble(0);
    }
    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        String currency = getCurrency();
        Double measure = (Double) underlyingMeasures[0];
        if (Currencies.EUR.getCurrency().equals(currency) || measure == null) {
            //no need for conversion
            return measure;
        }
        Date date = (Date) LocationUtil.getCoordinate(leafLocation, dateLevelInfo);
        Pair forexKeys = new Pair<>("EUR/"+getCurrency(), date);
        final IQueryCache queryCache = getContext().get(IQueryCache.class);
        Double rate = (Double) queryCache.get(forexKeys);
        if (rate == null) {
            //the rate in not cached so we retrieve it from the datastore
            try {
                final Double rateRetrieved = getRate(getDatastoreVersion(), forexKeys);
                if (rateRetrieved == null) {
                    logger.warning("The Forex Rate for " + forexKeys + "is missing! The cube will return null as result for conversion");
                    return null;
                }
                queryCache.putIfAbsent(forexKeys, rateRetrieved);
                rate = rateRetrieved;
            } catch (RuntimeException e) {
                throw new QuartetRuntimeException("Exception in Forex Post-Processor : ", e);
            }
        }
        return measure * rate;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
