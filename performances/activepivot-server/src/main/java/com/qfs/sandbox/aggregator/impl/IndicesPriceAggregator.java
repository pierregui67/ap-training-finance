package com.qfs.sandbox.aggregator.impl;

import com.qfs.agg.IAggregationFunction;
import com.qfs.agg.impl.AGenericAggregationFunction;
import com.qfs.store.Types;
import com.quartetfs.fwk.QuartetPluginValue;

@QuartetPluginValue(intf = IAggregationFunction.class)
public class IndicesPriceAggregator extends AGenericAggregationFunction<Double, Double> {

    public final static String KEY = "PRICE";

    //private IActivePivotConfig apConfig = null;

    public IndicesPriceAggregator() {
        super(KEY, Types.TYPE_OBJECT);
    }

    @Override
    protected Double cloneAggregate(Double aDouble) {
        return aDouble;
    }

    @Override
    protected Double aggregate(boolean b, Double aggregate, Double inputValue) {
        /*if (this.apConfig == null)
            this.apConfig = APBean.getApConfig();*/
        if (inputValue == null)
            return aggregate;
        if (aggregate == null) {
            assert !b : "One cannot disaggregate from a null value.";
            return inputValue;
        }
        return aggregate;
    }

    @Override
    protected Double merge(boolean b, Double aDouble, Double aggregateType1) {
        return aggregate(b, aDouble, aggregateType1);
    }
}
