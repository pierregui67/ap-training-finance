package com.qfs.sandbox.tuplepublisher.impl;

import com.qfs.msg.csv.translator.impl.TupleTranslator;
import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.StoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.*;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.PORTFOLIOS_INDEX_NAME;

/**
 * First the portfolio store, secondly the gdaxi store.
 * @param <String>
 */
public class IndicesTuplePublisher<String> extends TuplePublisher {


    public IndicesTuplePublisher(IDatastore datastore, Collection stores) {
        super(datastore, stores);
    }

    @Override
    public void publish(IStoreMessage message, List tuples) {
        Iterator storeIt = this.stores.iterator();
        //((TupleTranslator) ((StoreMessage) message).translator).columnIndex
        List<Object[]> portfolioTuples = new ArrayList<Object[]>();
        List<Object[]> gdaxiTuples = new ArrayList<Object[]>();

        //int indIndexName = ((TupleTranslator) ((StoreMessage) message).messageHandler).columnIndex.get(PORTFOLIOS_INDEX_NAME);

        // TODO : use the map !

        int cpt = 0;
        while (cpt < tuples.size()) {
            Object[] currentTuple = (Object[]) tuples.get(cpt);
            Object[] portfolio = new Object[] {currentTuple[5], currentTuple[0], currentTuple[7], currentTuple[3],
                    currentTuple[6]};
            Object[] gdaxi = new Object[] {currentTuple[0], currentTuple[1], currentTuple[2], currentTuple[3],
                    currentTuple[4], currentTuple[5]};
            portfolioTuples.add(portfolio);
            gdaxiTuples.add(gdaxi);
            cpt++;
        }

        Collection<Object[]> processedPortfolio = this.process(message, portfolioTuples);
        Collection<Object[]> processedGdaxi = this.process(message, gdaxiTuples);

        List storesList = (List) this.stores;

        this.datastore.getTransactionManager().addAll((java.lang.String) storesList.get(0), processedPortfolio);
        this.datastore.getTransactionManager().addAll((java.lang.String) storesList.get(1), processedGdaxi);
    }
}
