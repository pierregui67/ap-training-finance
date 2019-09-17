package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Properties;

@SuppressWarnings("serial")
@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = BasicAllMemberHide.PLUGIN_KEY)
public class BasicAllMemberHide extends ABasicPostProcessor<Object> {

    public static final String PLUGIN_KEY = "HIDE";
    private IHierarchyInfo currencyHierarchy;
    public BasicAllMemberHide(final String name, final IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }
    @Override
    public void init(final Properties properties) throws QuartetException {
        super.init(properties);
        //Retrieve the currency hierarchy's 'info', an object for
        //interrogating the contents of a location in that hierarchy
        String currencyHierarchyDescription = properties.getProperty("hierarchy", "Currency");
        currencyHierarchy = HierarchiesUtil.getHierarchy(pivot, currencyHierarchyDescription).getHierarchyInfo();
    }
    /**
     * The values of all the underlying measure specified in the 'underlyingMeasures' attribute of the
     * postProcessor element in the cube definition xml will be available in the underlying measures array
     */
    @Override
    public Object evaluate(final ILocation location, final Object[] underlyingMeasures) {
        double value = (double)underlyingMeasures[0];

        if (isAtAllMember(location)) {
            return null;
        }
        //we only handle one underlying measure here
        return underlyingMeasures[0];
    }
    private boolean isAtAllMember(ILocation location) {
        //        //The first level is the AllMember level therefore
        //        //If the location is deeper than this, we know that
        //it does in fact express the currency and is interesting to us
        int test = LocationUtil.getDepth(location, currencyHierarchy);
        return LocationUtil.getDepth(location, currencyHierarchy) == 1;
    }
    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
