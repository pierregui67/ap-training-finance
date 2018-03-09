package com.qfs.sandbox.postprocessor.impl;

import com.qfs.store.record.IRecordBlock;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.postprocessing.streams.impl.AStoreStream;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousQueryEngine;
import com.quartetfs.biz.pivot.query.aggregates.IStream;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.HashSet;
import java.util.Set;

@QuartetExtendedPluginValue(intf = IStream.class, key = ForexStream.PLUGIN_KEY)
public class ForexStream extends AStoreStream<Set<String>, Set<String>>{
    /** plugin key */
    public static final String PLUGIN_KEY = "DISPLAY_FOREX";

    /**
     * Constructor
     *
     * @param engine The {@link IAggregatesContinuousQueryEngine continuous query engine} that
     *               created it
     * @param pivot  The {@link IMultiVersionActivePivot pivot} it belongs to
     */
    public ForexStream(IAggregatesContinuousQueryEngine engine, IMultiVersionActivePivot pivot) {
        super(engine, pivot);
    }

    @Override
    protected Set<String> createNew() {
        return null;
    }

    @Override
    protected void collectAdded(IRecordBlock<IRecordReader> records, Set<String> collector) {
        addEvent(records, collector);
    }

    private void addEvent(IRecordBlock<IRecordReader> records, Set<String> collector) {
        for (IRecordReader r : records) {
            addEvent(r, collector);
        }
    }

    private void addEvent(IRecordReader records, Set<String> collector) {
        // TODO !
        System.out.println("Pierre qui roule n'amasse pas mousse !");
    }

    @Override
    protected void collectDeleted(IRecordBlock<IRecordReader> records, Set<String> collector) {
        addEvent(records, collector);

    }

    @Override
    protected void collectUpdated(IRecordBlock<IRecordReader> oldValues, IRecordBlock<IRecordReader> newValues, Set<String> collector) {
        addEvent(newValues, collector);
    }

    @Override
    protected Set<String> toEvent(Set<String> collector) {
        return null;
    }

    @Override
    public Class<Set<String>> getEventType() {
        Set<String> set = new HashSet<String>();
        return (Class<Set<String>>) set.getClass();
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }
}
