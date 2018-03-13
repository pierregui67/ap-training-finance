package com.qfs.sandbox.postprocessor.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.biz.pivot.query.IQueryCache;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ADynamicAggregationPostProcessor{

    public static final String PLUGIN_KEY="FOREX";

    public final static String REFERENCE_CURRENCY = "referenceCurrency";
    protected String referenceCurrency;

    /**
     * Constructor.
     *
     * @param name            The name of the post-processor instance.
     * @param creationContext The creation context that contains all additional parameters for the post-processor construction.
     */
    public ForexPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        // Init the properties
        if (properties.containsKey(REFERENCE_CURRENCY)) {
            this.referenceCurrency = properties.getProperty(REFERENCE_CURRENCY);
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + REFERENCE_CURRENCY);
        }

        /*ICursor cursor = getDatastoreVersion().getQueryRunner().forStore(FOREX_STORE_NAME).withoutCondition().withResultsLimit(1).selectingAllReachableFields().run();
        if (cursor.hasNext()) {
            cursor.next();
            IRecordReader reader = cursor.getRecord();
            this.referenceCurrency = (String) reader.read(FOREX_INITIAL_CURRENCY);
        }*/

        // Declaring the dependency to the context values
        addContextDependency(ICurrencyContextValue.class);
    }

    String getCurrency() {
        ICurrencyContextValue currencyContext = pivot.getContext().get(ICurrencyContextValue.class);
        if (currencyContext == null) {
            throw new QuartetRuntimeException("Cannot retrieve the currency context from post-processor " + getType());
        }
        return currencyContext.getCurrency();
    }

    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Double value = (Double) underlyingMeasures[0];
        Double rate;
        IQueryCache queryCache = pivot.getContext().get(IQueryCache.class);
        rate = (Double) queryCache.get(getCurrency());
        if(rate == null) {
            Double computed = getRateByLaunchingQuery();
            Double concurrent = (Double) queryCache.putIfAbsent(getCurrency(), computed);
            rate = concurrent == null ? computed : concurrent;
        }
        return rate *  value;
    }

    protected Double getRateByLaunchingQuery() {
        if (getCurrency().equals(referenceCurrency))
            return 1.0;
        IRecordReader record = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE_NAME,
                new Object[] {referenceCurrency, getCurrency()}, FOREX_RATE);
        if (record != null) {
            return (Double) record.read(FOREX_RATE);
        }
        throw new IllegalArgumentException("The query to the forex store does not return valid result. Check the currency validity.");
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}
