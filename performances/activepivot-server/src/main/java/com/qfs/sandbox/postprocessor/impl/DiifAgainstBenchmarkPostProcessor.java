package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.function.BiFunction;

@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = DiifAgainstBenchmarkPostProcessor.PLUGIN_KEY)
public class DiifAgainstBenchmarkPostProcessor extends PerformanceAgainstBenchmarkPostProcessor {

    public static final String PLUGIN_KEY = "DIFF_PERFORMANCE";

    public DiifAgainstBenchmarkPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected BiFunction<Double, Double, Double> setPerformanceFunction() {
        return new BiFunction<Double, Double, Double>() {
            @Override
            public Double apply(Double benchmark, Double portfolio) {
                return portfolio - benchmark;
            }
        };
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
