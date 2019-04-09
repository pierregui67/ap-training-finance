package com.qfs.sandbox.cfg.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.ITuplePublisher;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.QuartetRuntimeException;
import javafx.util.Pair;

import java.util.*;

import static java.lang.Math.max;


public class CustomTuplePublisher<I> implements ITuplePublisher<I> {

    //STORE NAME

    private static final String HISTORY_STORE_NAME = "HistoryStore";
    private static final String BASE_STORE_NAME = "BaseStore";

    //STORE FIELD
    private static final String HISTORY_STOCK_SYMBOL = "HistoryStockSymbol";
    private static final String HISTORY_DATE = "HistoryDate";

    private static final String PORTFOLIOS_DATE = "PortfoliosDate";
    private static final String PORTFOLIOS_STOCK_SYMBOL = "PortfoliosStockSymbol";

    protected final IDatastore datastore;
    protected final Collection<String> stores;
    protected final boolean removal;

    public CustomTuplePublisher(IDatastore datastore, Collection<String> stores) {
        this(datastore, stores, false);
    }

    public CustomTuplePublisher(IDatastore datastore, String store) {
        this(datastore, (Collection) Collections.singleton(store), false);
    }

    public CustomTuplePublisher(IDatastore datastore, String store, boolean removal) {
        this.datastore = datastore;
        this.stores = Collections.singleton(store);
        this.removal = removal;
    }

    public CustomTuplePublisher(IDatastore datastore, Collection<String> stores, boolean removal) {
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
        int stockIndex = (store == HISTORY_STORE_NAME) ?
                datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(HISTORY_STOCK_SYMBOL)
                : datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(PORTFOLIOS_STOCK_SYMBOL);
        int dateIndex = (store == HISTORY_STORE_NAME) ?
                datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(HISTORY_DATE)
                : datastore.getSchemaMetadata().getStoreMetadata(store).getStoreFormat().getRecordFormat().getFieldIndex(PORTFOLIOS_DATE);

        Iterator<Object[]> iterator = originalTuples.iterator();
        while (iterator.hasNext()) {
            Object[] o = iterator.next();
            //if (stockIndex == 3) {System.out.println(o[1]);}
            //if (o.length >= max(stockIndex,dateIndex)) {}
            basicStoreTuples.add(new Object[]{o[stockIndex], o[dateIndex]});
        }
        this.datastore.getTransactionManager().addAll(BASE_STORE_NAME, basicStoreTuples);
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


