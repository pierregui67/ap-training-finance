package com.qfs.sandbox.cfg.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.ITuplePublisher;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.QuartetRuntimeException;

import java.util.*;

import static java.lang.Math.max;


public class IndexTuplePublisher<I> implements ITuplePublisher<I> {

    //STORE NAME
    private static final String PORTFOLIOS_STORE_NAME ="PortfoliosStore";

    //STORE FIELD
    private static final String INDEX_STOCK_SYMBOL = "IndexStockSymbol";
    private static final String INDEX_COMPANY_NAME = "IndexCompanyName";
    private static final String INDEX_STOCK_VALUE = "IndexStockValue";
    private static final String INDEX_NAME = "IndexName";
    private static final String INDEX_DATE = "IndexDate";
    private static final String INDEX_POSITION_TYPE = "PositionType";
    private static final String INDEX_ID = "IndexId";
    private static final String INDEX_NUMBER = "IndexNumber";

    protected final IDatastore datastore;
    protected final Collection<String> stores;
    protected final boolean removal;

    public IndexTuplePublisher(IDatastore datastore, Collection<String> stores) {
        this(datastore, stores, false);
    }

    public IndexTuplePublisher(IDatastore datastore, String store) {
        this(datastore, (Collection) Collections.singleton(store), false);
    }

    public IndexTuplePublisher(IDatastore datastore, String store, boolean removal) {
        this.datastore = datastore;
        this.stores = Collections.singleton(store);
        this.removal = removal;
    }

    public IndexTuplePublisher(IDatastore datastore, Collection<String> stores, boolean removal) {
        this.datastore = datastore;
        this.stores = stores;
        this.removal = removal;
    }

    public Collection<String> getTargetStores() {
        return Collections.unmodifiableCollection(this.stores);
    }


    public void publish(IStoreMessage<? extends I, ?> message, List<Object[]> tuples) {
        Collection<Object[]> processedTuples = this.process(message, tuples);
        Iterator var4;
        String store;
        if (this.removal) {
            var4 = this.stores.iterator();

            while (var4.hasNext()) {
                store = (String) var4.next();
                List extractedKeyFields = StoreUtils.extractKeyFields(this.datastore.getSchemaMetadata(), store, processedTuples);

                try {
                    this.datastore.getTransactionManager().removeAll(store, extractedKeyFields);
                } catch (DatastoreTransactionException var8) {
                    throw new QuartetRuntimeException(var8);
                }
            }
        } else {
            var4 = this.stores.iterator();

            while (var4.hasNext()) {
                store = (String) var4.next();
                this.datastore.getTransactionManager().addAll(store, processedTuples);
                basicStorePublisher(store, processedTuples); //publish stock and date in baseStore
            }
        }
    }

    private void basicStorePublisher(String store, Collection<Object[]> originalTuples) {
        Collection<Object[]> basicStoreTuples = new ArrayList<>();
        int stockIndex = datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(INDEX_STOCK_SYMBOL);
        int dateIndex =  datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(INDEX_DATE);
        int numberIndex = datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(INDEX_NUMBER);
        int nameIndex =  datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(INDEX_NAME);
        int typeIndex =  datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(INDEX_POSITION_TYPE);
        Iterator<Object[]> iterator = originalTuples.iterator();
        while (iterator.hasNext()) {
            Object[] o = iterator.next();
            basicStoreTuples.add(new Object[]{o[dateIndex], o[nameIndex], o[numberIndex], o[stockIndex], o[typeIndex]});
        }
        this.datastore.getTransactionManager().addAll(PORTFOLIOS_STORE_NAME, basicStoreTuples);
    }


    public IDatastore getDatastore() {
        return this.datastore;
    }

    public boolean isRemoval() {
        return this.removal;
    }

    public List<Object[]> process(IStoreMessage<? extends I, ?> message, List<Object[]> tuples) {
        return tuples;
    }
}


