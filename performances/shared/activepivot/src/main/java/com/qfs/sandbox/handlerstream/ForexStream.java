package com.qfs.sandbox.handlerstream;

import com.qfs.store.record.IRecordBlock;
import com.qfs.store.record.IRecordReader;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.postprocessing.streams.impl.AStoreStream;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousQueryEngine;
import com.quartetfs.biz.pivot.query.aggregates.IStream;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE;

@QuartetExtendedPluginValue(intf = IStream.class, key = ForexStream.PLUGIN_KEY)
public class ForexStream extends AStoreStream<Set,Set> {

    public static final String PLUGIN_KEY = "FOREX_STREAM";

    public ForexStream(IAggregatesContinuousQueryEngine engine, IMultiVersionActivePivot pivot) {
        super(engine, pivot);
        setStore(FOREX_STORE);
    }

    @Override
    protected Set createNew() {
        return new HashSet();
    }

    @Override
    protected void collectAdded(IRecordBlock<IRecordReader> records, Set collector) {
        collect(records, collector);
    }

    @Override
    protected void collectDeleted(IRecordBlock<IRecordReader> records, Set collector) {
        collect(records, collector);
    }

    @Override
    protected void collectUpdated(IRecordBlock<IRecordReader> oldValues, IRecordBlock<IRecordReader> newValues, Set collector) {
        collect(newValues, collector);
    }

    private void collect(IRecordBlock<IRecordReader> newValues, Set collector) {
        for (IRecordReader r : newValues) {
            collector.add((Date) dictionaries.getDictionary(1).read(r.readInt(1)));
        }
    }

    @Override
    protected Set toEvent(Set collector) {
        Set eventToBeSent = new HashSet<>();
        for (Object e : collector) {
            eventToBeSent.add(e);
        }
        return eventToBeSent;
    }


    @Override
    public String getType() {
        return PLUGIN_KEY;
    }

    @Override
    public Class<Set> getEventType() {
        Set<Object> set = new HashSet<>();
        return (Class<Set>) set.getClass();
    }
}
