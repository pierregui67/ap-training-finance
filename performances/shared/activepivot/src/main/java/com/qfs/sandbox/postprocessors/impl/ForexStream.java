package com.qfs.sandbox.postprocessors.impl;

import com.qfs.sandbox.cfg.impl.DatastoreConfig;
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
public class ForexStream extends AStoreStream<Set<String>,Set<String>> {

    public static final String PLUGIN_KEY = "FOREX_STREAM";
    private static final long serialVersionUID = 201804241531L;




    public ForexStream(final IAggregatesContinuousQueryEngine engine, final IMultiVersionActivePivot pivot) {
        super(engine, pivot);
        setStore(DatastoreConfig.FOREX_STORE_NAME);
    }

    @Override
    protected Set<String> createNew() {
        return new HashSet<String>();
    }

    @Override
    public Class<Set<String>> getEventType() {

        Set<String> set = new HashSet<String>();
        return (Class<Set<String>>) set.getClass();
    }

    @Override
    protected void collectDeleted(IRecordBlock<IRecordReader> records, Set<String> collector) {
        addEvent(records, collector);
    }

    @Override
    protected void collectAdded(IRecordBlock<IRecordReader> records, Set<String> collector) {
        addEvent(records, collector);
    }

    private void addEvent(IRecordReader record, Set<String> events){
        events.add((String) dictionaries.getDictionary(1).read(record.readInt(1)));
    }

    private void addEvent(IRecordBlock<IRecordReader> records, Set<String> collector){
        for (IRecordReader r : records){
            addEvent(r, collector);
        }
    }

    @Override
    protected void collectUpdated(IRecordBlock<IRecordReader> oldValues, IRecordBlock<IRecordReader> newValues, Set<String> collector) {
        addEvent(newValues, collector);
    }

    @Override
    protected Set<String> toEvent(Set<String> collector) {
        // TODO : understand !
        Set<String> eventToBeSent = new HashSet<>();
        for (String e : collector)
            eventToBeSent.add(e);
        return eventToBeSent;
    }



    @Override
    public String getType() {
        return PLUGIN_KEY;
    }


}
