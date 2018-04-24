package com.qfs.sandbox.postprocessors.impl;

import com.qfs.condition.ICondition;
import com.qfs.condition.impl.BaseConditions;
import com.qfs.sandbox.cfg.impl.DatastoreConfig;
import com.qfs.sandbox.context.impl.IReferenceCurrency;
import com.qfs.sandbox.context.impl.ReferenceCurrency;
import com.qfs.store.IDatastoreVersion;
import com.qfs.store.query.ICursor;
import com.qfs.store.query.IDictionaryCursor;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;

import javax.xml.crypto.Data;
import java.util.Properties;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ForexPostProcessor.PLUGIN_KEY)
public class ForexPostProcessor extends ABasicPostProcessor<Double> {
    public static final String PLUGIN_KEY = "FOREX";

    private IReferenceCurrency referenceCurrency;
    private IReferenceCurrency targetCurrency;

    public static final String REFERENCE_CURRENCY = "referenceCurrency";
    public static final String TARGET_CURRENCY = "targetCurrency";

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

            while(cursorCurrency.hasNext()){
                cursorCurrency.next();
                ratio = (Double) cursorCurrency.getRecord().read(DatastoreConfig.FOREX__RATIO);
            }
        }else {
            ratio = 1.0;
        }

        return ratio;
    }

    private double getRatioTwoCurrency(String referenceCurrency, String targetCurrency){

        Double ratio = 0.0;
        if(!referenceCurrency.equals("EUR")){
            ratio = getRatio(targetCurrency)/getRatio(referenceCurrency);
        }else{
            ratio = getRatio(targetCurrency);
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
