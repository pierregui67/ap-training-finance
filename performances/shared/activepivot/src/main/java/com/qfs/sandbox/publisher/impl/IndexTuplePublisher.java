package com.qfs.sandbox.publisher.impl;

import com.qfs.msg.csv.IFileInfo;
import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;
import com.qfs.store.transaction.DatastoreTransactionException;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

public class IndexTuplePublisher extends TuplePublisher<IFileInfo<String>>{

    private static final Logger LOGGER = Logger.getLogger(IndexTuplePublisher.class.getSimpleName());

    private boolean autocommit = false;

    public IndexTuplePublisher(IDatastore datastore, Collection<String> stores) {
        super(datastore, stores);
    }

    public IndexTuplePublisher(IDatastore datastore, Collection<String> stores, boolean autocommit) {
        super(datastore, stores);
        this.autocommit = autocommit;
    }

    @Override
    public void publish(IStoreMessage<? extends IFileInfo<String>, ?> message, List<Object[]> tuples) {
        Collection<Object[]> indexTuples = new HashSet<>();
        Collection<Object[]> customData = new HashSet<>();
        for (Object[] tuple : tuples) {
            //|COMPANY|PRICE|SYMBOL|IDENTIFIER|TYPE|DATE|VOLUME
            String index = (String) message.read(PORTFOLIO_TYPE, tuple);
            String company = (String) message.read(COMPANY, tuple);
            Double price = (Double) message.read(CLOSE_PRICE, tuple);
            String symbol = (String) message.read(STOCK_SYMBOL, tuple);
            String id = (String) message.read(IDENTIFIER, tuple);
            String type = (String) message.read(POSITION_TYPE, tuple);
            Date date = (Date) message.read(DATE, tuple);
            int volume = (int) message.read(QUANTITY, tuple);
            indexTuples.add(new Object[] {date, index, volume, symbol, type});
            customData.add(new Object[] {symbol, company, price, id});
        }

        //start transcation if needed
        if (autocommit) {
            startTransaction();
        }
        //push data to the cube
        getDatastore().getTransactionManager().addAll(PORTFOLIOS_STORE, indexTuples);
        getDatastore().getTransactionManager().addAll(CUSTOM_INDEX_DATA_STORE, customData);
        //commit transaction if needed
        if (autocommit) {
            commitTransaction();
        }
    }

    private void startTransaction() {
        try {
            getDatastore().getTransactionManager().startTransaction();
        } catch (DatastoreTransactionException e) {
            LOGGER.warning("An error occured while publishing Index with publisher, will roll back " +  e.getMessage());
        }
    }

    private  void commitTransaction() {
        try{
            getDatastore().getTransactionManager().commitTransaction();
        } catch (DatastoreTransactionException e) {
            LOGGER.warning("An error occured while publishing Index with publisher, will roll back" + e.getMessage());
            try {
                getDatastore().getTransactionManager().rollbackTransaction();
            } catch (DatastoreTransactionException e1) {
                LOGGER.severe("An error occured while rolling back current transaction for Index with publisher" +e.getMessage());
            }
        }
    }

}
