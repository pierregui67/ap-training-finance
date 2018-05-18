package com.qfs.sandbox.postprocesseur.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@QuartetExtendedPluginValue(intf=IPostProcessor.class, key= VaRPostProcessor.PLUGIN_KEY)
public class VaRPostProcessor extends ADynamicAggregationPostProcessor<Double, ArrayList<Double>> {

    public static final String PLUGIN_KEY = "VAR";
    private static final int NUMBER_ITERATION = 500;

    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public VaRPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected Double evaluateLeaf(ILocation leafLocation, Object[] underlyingMeasures) {
        ArrayList<Double> initialList = (ArrayList<Double>) underlyingMeasures[0];
        if (initialList == null)
            return null;
        if (initialList.size() < 50) {
            return null;
        }
        Double sum = 0.0;
        Double portfolioValue = (Double) underlyingMeasures[1];
        if (portfolioValue == null)
            return null;

        for (int i = 0; i < NUMBER_ITERATION; i++) {
            ArrayList<Double> sortedList = buildListByBootstrap((ArrayList<Double>) underlyingMeasures[0]);
            Collections.sort(sortedList);
            sum += sortedList.get(4);

        }
        Double percent = (sum / NUMBER_ITERATION) / portfolioValue;
        if (percent > 0)
            System.out.println("Percent = " + percent);
        return  percent;
    }

    private ArrayList<Double> buildListByBootstrap(ArrayList<Double> list) {
        ArrayList<Double> bootstrappedList = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            Random rand = new Random();
            int random = rand.nextInt(list.size());
            bootstrappedList.add(list.get(random));
        }
        return bootstrappedList;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
