package com.qfs.sandbox.cfg.impl;

import com.qfs.store.IDatastore;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor1.PLUGIN_KEY)
public class ForexPostProcessor1 extends ABasicPostProcessor {

    @Autowired
    protected IDatastore datastore;
    private static final String FOREX_STORE_NAME = "ForexStore";
    public final static String PLUGIN_KEY = "ForexPP1";
    private static final String FOREX_RATE = "ForexRate";
    private static final String FOREX_CURRENCY_PAIR = "CurrencyPair";
    private Double rate;

    public ForexPostProcessor1(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
        addContextDependency(CurrencyContextValue.class);
    }

    @Override
    public Object evaluate(ILocation location, Object[] underlyingMeasures) {
        String date = "";
        if (getCurrency().equals("EUR")) rate = 1.0;
        else {
            Object[] key = new Object[]{"EUR/" + getCurrency(), date};
            IRecordReader record = DatastoreQueryHelper.getByKey(datastore.getMostRecentVersion(), FOREX_STORE_NAME, key, FOREX_RATE);
            if (record== null) {
                throw new QuartetRuntimeException("Cannot find the record of the keys " + "EUR/" + getCurrency());
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
