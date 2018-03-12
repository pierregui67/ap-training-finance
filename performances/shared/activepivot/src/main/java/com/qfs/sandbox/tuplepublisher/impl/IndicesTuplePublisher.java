package com.qfs.sandbox.tuplepublisher.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.*;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

/**
 * First the portfolio store, secondly the gdaxi store.
 * @param <String>
 */
public class IndicesTuplePublisher<String> extends TuplePublisher {

    Map<String, Integer> nameToColumnIndex;

    public IndicesTuplePublisher(IDatastore datastore, Collection stores, Map map) {
        super(datastore, stores);
        this.nameToColumnIndex = map;
    }

    @Override
    public void publish(IStoreMessage message, List tuples) {
        Iterator storeIt = this.stores.iterator();
        //((TupleTranslator) ((StoreMessage) message).translator).columnIndex
        List<Object[]> portfolioTuples = new ArrayList<Object[]>();
        List<Object[]> gdaxiTuples = new ArrayList<Object[]>();

        int indDate = nameToColumnIndex.get(PORTFOLIOS_DATE);
        int indIndName = nameToColumnIndex.get(PORTFOLIOS_INDEX_NAME);
        int indNumStock = nameToColumnIndex.get(PORTFOLIOS_NUMBER_STOCKS);
        int indStockSym = nameToColumnIndex.get(PORTFOLIOS_STOCK_SYMBOL);
        int indPosType = nameToColumnIndex.get(PORTFOLIOS_POSITION_TYPE);
        int indCl = nameToColumnIndex.get(INDICES_CLOSE_VALUE);
        int indId = nameToColumnIndex.get(INDICES_IDENTIFIER);
        int indName = nameToColumnIndex.get(INDICES_NAME_COMPANY);

        int cpt = 0;
        while (cpt < tuples.size()) {
            Object[] currentTuple = (Object[]) tuples.get(cpt);
            Object[] portfolio = new Object[] {currentTuple[indDate], currentTuple[indIndName], currentTuple[indNumStock],
                    currentTuple[indStockSym], currentTuple[indPosType]};
            Object[] gdaxi = new Object[] {currentTuple[indIndName], currentTuple[indName], currentTuple[indCl],
                    currentTuple[indStockSym], currentTuple[indId], currentTuple[indDate]};
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
