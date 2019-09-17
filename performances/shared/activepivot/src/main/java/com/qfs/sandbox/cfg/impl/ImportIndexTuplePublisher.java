package com.qfs.sandbox.cfg.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.ITuplePublisher;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ImportIndexTuplePublisher<I> implements ITuplePublisher<I> {

    @Override
    public void publish(IStoreMessage<? extends I, ?> iStoreMessage, List<Object[]> list) {

    }
}