package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IMeasuresProvider;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.impl.UnderlyingMeasuresPrefetcher;
import java.util.ArrayList;
import java.util.Collection;


public class CustomPrefetcher extends UnderlyingMeasuresPrefetcher {
    protected IHierarchy portfoliosHierarchy;

    public CustomPrefetcher(IMeasuresProvider measuresProvider, String... underlyingMeasures) {
        super(measuresProvider, underlyingMeasures);
    }

    public CustomPrefetcher(IMeasuresProvider measuresProvider,IHierarchy hierarchy, String... underlyingMeasures) {
        super(measuresProvider, underlyingMeasures);
        portfoliosHierarchy = hierarchy;
    }


    @Override
    protected Collection<ILocation> computeLocations(Collection<ILocation> locations) {
        ArrayList<ILocation> listLocation = new ArrayList<>();
        for (ILocation location : locations) {
            listLocation.add(LocationUtil.createModifiedLocation(location, portfoliosHierarchy.getHierarchyInfo(), new Object[]{ILevel.ALLMEMBER, null}));
        }
        return listLocation;
    }
}

