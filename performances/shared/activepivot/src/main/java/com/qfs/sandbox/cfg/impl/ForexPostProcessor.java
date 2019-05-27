package com.qfs.sandbox.cfg.impl;

import com.qfs.store.IDatastore;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.context.IActivePivotContext;
import com.quartetfs.biz.pivot.context.impl.ActivePivotContext;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ADynamicAggregationPostProcessor {
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */



    private static final String FOREX_STORE_NAME = "ForexStore";
    public final static String PLUGIN_KEY = "ForexPP";
    private static final String FOREX_RATE = "ForexRate";
    private static final String FOREX_CURRENCY_PAIR = "CurrencyPair";
    private Double rate;

    public ForexPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
        addContextDependency(ICurrencyContextValue.class);
    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);
        addContextDependency(ICurrencyContextValue.class);
    }
    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        IDatastore datastore;
        Date date = (Date)leafLocation.getCoordinate(2,0);
        if (getCurrency().equals("EUR")) rate = 1.0;
        else {
            Object[] key = new Object[]{"EUR/" + getCurrency(), date};
            IRecordReader record = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE_NAME, key, FOREX_RATE);
            if (record== null) {
                return 0.0;
                //throw new QuartetRuntimeException("Cannot find the record of the keys " + "EUR/" + getCurrency() + "/" + date);
            }
            rate =  (Double) record.read(FOREX_RATE);
        }
        return  (Double) underlyingMeasures[0] * rate;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    public String getCurrency() {
        ICurrencyContextValue currencyContext = pivot.getContext().get(ICurrencyContextValue.class);
        if (currencyContext == null) {
            throw new QuartetRuntimeException("Cannot retrieve the currency context from post-processor " + getType());
        }
        return currencyContext.getCurrency();
    }
}
