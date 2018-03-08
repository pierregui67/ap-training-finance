package com.qfs.sandbox.postprocessor.impl;

import com.qfs.sandbox.context.IReferenceCurrency;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.PostProcessorInitializationException;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Properties;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key=ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ADynamicAggregationPostProcessor{

    public static final String PLUGIN_KEY="FOREX";

    public final static String INITIAL_CURRENCY = "initialCurrency";
    protected String initialCurrency;

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
        if (properties.containsKey(INITIAL_CURRENCY)) {
            this.initialCurrency = properties.getProperty(INITIAL_CURRENCY);
        } else {
            throw new PostProcessorInitializationException("Post processor " + getName() + " is missing the mandatory property " + INITIAL_CURRENCY);
        }

        // Declaring the dependency to the context values
        addContextDependency(IReferenceCurrency.class);
    }

    String getCurrency() {
        IReferenceCurrency referenceCurrencyContext = pivot.getContext().get(IReferenceCurrency.class);
        if (referenceCurrencyContext == null) {
            throw new QuartetRuntimeException("Cannot retrieve the confidence level context from post-processor " + getType());
        }
        return referenceCurrencyContext.getCurrency();
    }

    @Override
    protected Object evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        Double value = (Double) underlyingMeasures[0];
        if (getCurrency().equals(initialCurrency))
            return value;
        Double rate;
        IRecordReader record = DatastoreQueryHelper.getByKey(getDatastoreVersion(), FOREX_STORE_NAME,
                new Object[] {initialCurrency, getCurrency()}, FOREX_RATE);
        if (record != null) {
            rate = (Double) record.read(FOREX_RATE);
            return rate *  value;
        }
        throw new IllegalArgumentException("The query to the forex store does not return valid result. Check the currency validity.");
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}
