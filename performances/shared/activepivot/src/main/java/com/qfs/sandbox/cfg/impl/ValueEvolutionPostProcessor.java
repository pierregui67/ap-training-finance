package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = ValueEvolutionPostProcessor.PLUGIN_KEY)
public class ValueEvolutionPostProcessor extends ABasicPostProcessor<Double> {

    public static final String PLUGIN_KEY = "VALUE_EVOLUTION";
    /**
     * Constructor
     *
     * @param name            The name of the post-processor
     * @param creationContext The {@link IPostProcessorCreationContext creation context} of this post-processor.
     */
    public ValueEvolutionPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    public Double evaluate(ILocation location, Object[] underlyingMeasures) {
        for (Object o : underlyingMeasures){
            if (o == null){
                return 0.0;
            }
        }
        final double current = ((Number) underlyingMeasures[0]).doubleValue();
        final double previous = ((Number) underlyingMeasures[1]).doubleValue();
        if(current == 0.0){return 0.0;}
        else return  (current - previous) /previous;
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
