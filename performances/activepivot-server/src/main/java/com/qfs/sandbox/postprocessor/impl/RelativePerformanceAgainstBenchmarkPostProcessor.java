package com.qfs.sandbox.postprocessor.impl;

import com.quartetfs.biz.pivot.cube.hierarchy.measures.IPostProcessorCreationContext;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.function.BiFunction;


@QuartetExtendedPluginValue(intf = IPostProcessor.class, key = RelativePerformanceAgainstBenchmarkPostProcessor.PLUGIN_KEY)
public class RelativePerformanceAgainstBenchmarkPostProcessor extends PerformanceAgainstBenchmarkPostProcessor{

    public static final String PLUGIN_KEY = "RELATIVE_PERFORMANCE";

    public RelativePerformanceAgainstBenchmarkPostProcessor(String name, IPostProcessorCreationContext creationContext) {
        super(name, creationContext);
    }

    @Override
    protected BiFunction<Double, Double, Double> setPerformanceFunction() {
        return new BiFunction<Double, Double, Double>() {
            @Override
            public Double apply(Double benchmark, Double portfolio) {
                return (portfolio - benchmark) / benchmark * 100;
            }
        };
    }

    @Override
    public String getType() {
        return PLUGIN_KEY;
    }
}
