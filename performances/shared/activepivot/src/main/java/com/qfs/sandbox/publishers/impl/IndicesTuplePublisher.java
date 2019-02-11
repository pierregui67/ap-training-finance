package com.qfs.sandbox.publishers.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

public class IndicesTuplePublisher extends TuplePublisher {

    private static final int NUMBER_COLUMN = 6;

    Map<String, Integer> map;

    public IndicesTuplePublisher(IDatastore datastore, Collection stores, Map<String, Integer> map) {
        super(datastore, stores);
        this.map = map;
    }

    @Override
    public void publish(IStoreMessage message, List tuples) {
        List<Object[]> portfolioTuples = new ArrayList<Object[]>();
        List<Object[]> indicesTuples = new ArrayList<Object[]>();
        tuples.stream().forEach(
                elmt -> {
                    Object[] temp = (Object[]) elmt;
                    Object[] portfolioTemp = new Object[]{
                            temp[map.get(INDICES_DATE_TIME)],
                            temp[map.get(INDICES_INDEX_NAME)],
                            100, // number of stocks = 100,
                            temp[map.get(INDICES_STOCK_SYMB)],
                            temp[map.get(INDICES_EQUITY)]
                    };

                    Object[] indiceTemp = new Object[]{
                            temp[map.get(INDICES_INDEX_NAME)],
                            temp[map.get(INDICES_COMPANY_NAME)],
                            temp[map.get(INDICES_CLOSE_VALUE)],
                            temp[map.get(INDICES_STOCK_SYMB)],
                            temp[map.get(INDICES_EQUITY)],
                            temp[map.get(INDICES_DATE_TIME)]
                    };

                    portfolioTuples.add(portfolioTemp);
                    indicesTuples.add(indiceTemp);
                }
        );

        Collection<Object[]> processedPortfolio = this.process(message, portfolioTuples);
        Collection<Object[]> processedGdaxi = this.process(message, indicesTuples);
        this.datastore.getTransactionManager().addAll(((ArrayList<String>)this.stores).get(0), processedPortfolio);
        this.datastore.getTransactionManager().addAll(((ArrayList<String>)this.stores).get(1), processedGdaxi);
    }
}
