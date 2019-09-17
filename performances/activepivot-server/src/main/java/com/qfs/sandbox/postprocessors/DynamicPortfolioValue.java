package com.qfs.sandbox.postprocessors;

import com.qfs.store.Types;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@QuartetExtendedPluginValue(intf= IPostProcessor.class, key = DynamicPortfolioValue.PLUGIN_KEY)
public class DynamicPortfolioValue extends ADynamicAggregationPostProcessor<Double> {
    /** serialVersionUID */
    private static final long serialVersionUID = 15874126988574L;
    /** post processor plugin key */
    public final static String PLUGIN_KEY = "PORTFOLIOVALUE";
    /** currency level info */
    protected ILevelInfo currencyLevelInfo = null;
    public DynamicPortfolioValue(String name, final IPostProcessorCreationContext pivot) {
        super(name, pivot);
    }
    /** post processor initialisation */
    @Override
    public  void init(Properties properties) throws QuartetException {
        super.init(properties);
        // init required level values
        currencyLevelInfo = this.leafLevelsInfo.get(0);
    }
    /**
     * Perform the evaluation of the post processor on a leaf (as defined in the properties).
     * Here the leaf level is the UnderlierCurrency level in the Underlyings hierarchy .
     */
    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {

        return (Integer)underlyingMeasures[0]*(Double)underlyingMeasures[1];
    }
    /**
     * @return the data type of the post processed values that must be dynamically aggregated.
     */
    @Override
    protected int getDataType() { return Types.TYPE_DOUBLE; }
    /**
     * @return the type of this post processor, within the post processor extended plugin.
     */
    @Override
    public String getType() { return PLUGIN_KEY; }
}
