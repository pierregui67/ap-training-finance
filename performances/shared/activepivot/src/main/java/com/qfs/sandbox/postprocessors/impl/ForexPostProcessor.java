package com.qfs.sandbox.postprocessors.impl;

import com.qfs.condition.ICondition;
import com.qfs.condition.impl.BaseConditions;
import com.qfs.sandbox.cfg.impl.DatastoreConfig;
import com.qfs.sandbox.context.IReferenceCurrency;
import com.qfs.sandbox.context.impl.ReferenceCurrency;
import com.qfs.store.IDatastoreVersion;
import com.qfs.store.query.ICursor;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.biz.pivot.query.IQueryCache;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ABasicPostProcessor<Double> {
    public static final String PLUGIN_KEY = "FOREX";

    private IReferenceCurrency referenceCurrency;
    private IReferenceCurrency targetCurrency;

    public static final String REFERENCE_CURRENCY = "referenceCurrency";
    public static final String TARGET_CURRENCY = "targetCurrency";

    IQueryCache queryCache;

    public ForexPostProcessor(String name, IPostProcessorCreationContext creationContext){
        super(name, creationContext);


    }

    @Override
    public void init(Properties properties) throws QuartetException {
        super.init(properties);

        addContextDependency(IReferenceCurrency.class);

        if (properties.containsKey(REFERENCE_CURRENCY)) {
            this.referenceCurrency = new ReferenceCurrency(properties.getProperty(REFERENCE_CURRENCY));
        }

        if(this.referenceCurrency == null){ // value from PerformanceAttributionCube.xml
            this.referenceCurrency = getContext().get(IReferenceCurrency.class);
        }

        if(this.referenceCurrency == null){
            throw new QuartetRuntimeException("Post processor "+ getType() +
                    " need to have referencyCurrency defined in cube schema or Measure");

        }

        if (properties.containsKey(TARGET_CURRENCY)){
            this.targetCurrency = new ReferenceCurrency(properties.getProperty(TARGET_CURRENCY));
        }


        if(this.targetCurrency == null){
            this.targetCurrency = new ReferenceCurrency();

        }

    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    private String getReferenceCurrency() {
        return this.referenceCurrency.getReferenceCurrency();
    }

    private String getTargetCurrency(){
        return this.targetCurrency.getReferenceCurrency();
    }

    private double getRatio(String currency){
        Double ratio = 0.0;

        if( !currency.equals("EUR")){
            IDatastoreVersion datastoreVersion = getDatastoreVersion();
            ICondition condition = BaseConditions.And(
                    BaseConditions.Equal(DatastoreConfig.FOREX__TARGET_CURRENCY, currency)
            );

            ICursor cursorCurrency = datastoreVersion.getQueryRunner()
                    .forStore(DatastoreConfig.FOREX_STORE_NAME)
                    .withCondition(condition)
                    .withResultsLimit(1)
                    .selecting(DatastoreConfig.FOREX__RATIO)
                    .run();

            if (cursorCurrency == null){
                throw new IllegalArgumentException("The query can't find the " + currency +
                        " currency in the datastore");
            }

            while(cursorCurrency.hasNext()){
                cursorCurrency.next();
                ratio = (Double) cursorCurrency.getRecord().read(DatastoreConfig.FOREX__RATIO);
            }
        }else {
            ratio = 1.0;
        }

        return ratio;
    }

    private double getRatioFromQueryOrCache(String currency){

        // code from here: http://support.quartetfs.com/confluence/display/AP5/IQueryCache

        queryCache = pivot.getContext().get(IQueryCache.class);

        Double ratio = (Double) queryCache.get(currency);
        if (ratio == null){
            Double computed = getRatio(currency);
            Double concurrent = (Double) queryCache.putIfAbsent(currency, computed);
            ratio = concurrent == null ? computed : concurrent;
        }

        return ratio;
    }

    protected double getRatioTwoCurrency(String referenceCurrency, String targetCurrency){

        Double ratio;


        if(!referenceCurrency.equals("EUR")){
            Double cur1 = getRatioFromQueryOrCache(referenceCurrency);
            Double cur2 = getRatioFromQueryOrCache(targetCurrency);
            ratio = cur2/cur1;
        }else{
            ratio = getRatioFromQueryOrCache(targetCurrency);
        }

        return ratio;
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {

        String referenceCurrency = getReferenceCurrency();
        String targetCurrency = getTargetCurrency();


        return (double) underlyingMeasures[0] * getRatioTwoCurrency(referenceCurrency, targetCurrency);
    }
}
