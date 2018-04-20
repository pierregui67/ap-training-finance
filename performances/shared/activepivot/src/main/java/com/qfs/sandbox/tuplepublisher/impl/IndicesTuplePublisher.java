package com.qfs.sandbox.tuplepublisher.impl;

import com.qfs.sandbox.cfg.impl.DatastoreConfig;
import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.*;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

public class IndicesTuplePublisher extends TuplePublisher {

//    private IDatastore indicesDatastore;
    private static final int NUMBER_COLUMN = 6;

    Map<String, Integer> map;

    public IndicesTuplePublisher(IDatastore datastore, ArrayList<String> stores, Map<String, Integer> map){
        super(datastore, stores);
        this.map = map;
    }

    @Override
    public void publish(IStoreMessage iStoreMessage, List list) {
        List<Object[]> portfolioTuples = new ArrayList<Object[]>();
        List<Object[]> indicesTuples = new ArrayList<Object[]>();
        list.stream().forEach(
                /*elmt ->{
                    Object[] temp = (Object[]) elmt;
                    Object[] res = new Object[NUMBER_COLUMN]; //

                    res[0] = temp[0];
                    res[1] = temp[3];
                    res[2] = temp[4];
                    res[3] = temp[5];
                    res[4] = temp[6];
                    res[5] = temp[7];


                    for (int i=0; i<NUMBER_COLUMN; i++){
                        ((Object[])elmt)[i] = res[i];
                    }
                }*/
                elmt -> {
                    Object[] temp = (Object[]) elmt;
                    Object[] portfolioTemp = new Object[]{
                            temp[map.get(INDICES__DATE_TIME)],
                            temp[map.get(INDICES__INDEX_NAME)],
                            100, // number of stocks = 100,
                            temp[map.get(INDICES__STOCK_SYMBOL)],
                            temp[map.get(INDICES__EQUITY)]
                    };

                    Object[] indiceTemp = new Object[]{
                            temp[map.get(INDICES__INDEX_NAME)],
                            temp[map.get(INDICES__COMPANY_NAME)],
                            temp[map.get(INDICES__CLOSE_VALUE)],
                            temp[map.get(INDICES__STOCK_SYMBOL)],
                            temp[map.get(INDICES__EQUITY)],
                            temp[map.get(INDICES__DATE_TIME)]
                    };

                    portfolioTuples.add(portfolioTemp);
                    indicesTuples.add(indiceTemp);
                }
        );

        Collection<Object[]> processedPortfolio = this.process(iStoreMessage, portfolioTuples);
        Collection<Object[]> processedGdaxi = this.process(iStoreMessage, indicesTuples);
        this.datastore.getTransactionManager().addAll(((ArrayList<String>)this.stores).get(0), processedPortfolio);
        this.datastore.getTransactionManager().addAll(((ArrayList<String>)this.stores).get(1), processedGdaxi);
    }

}
