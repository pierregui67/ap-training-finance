package com.qfs.sandbox.postprocessors;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IMeasuresProvider;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.impl.UnderlyingMeasuresPrefetcher;

import java.util.ArrayList;
import java.util.Collection;

public class LocationShiftPrefetcher extends UnderlyingMeasuresPrefetcher {
    protected String m_location2Shift;
    protected IHierarchyInfo m_level2shift;

    public LocationShiftPrefetcher(IMeasuresProvider measuresProvider, String location2Shift, IHierarchyInfo level2shift, String... underlyingMeasures) {
        super(measuresProvider, underlyingMeasures);
        m_level2shift = level2shift;
        m_location2Shift = location2Shift;
    }

    @Override
    protected Collection<ILocation> computeLocations(Collection<ILocation> locations) {
        Collection<ILocation> result = new LocationSet();
        result.addAll(locations);
        locations.forEach(x->{
            result.add(LocationUtil.createModifiedLocation(x, m_level2shift, new Object[] {ILevel.ALLMEMBER, m_location2Shift}));
        });
        return result;
    }

}
