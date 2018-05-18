package com.qfs.sandbox.tuplepublisher.impl;

import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.*;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.HISTORY_CLOSE;
import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.HISTORY_STOCK_SYMBOL;

public class HistoryTuplePublisher extends TuplePublisher {

    Map<String, Integer> nameToColumnIndex;

    public HistoryTuplePublisher(IDatastore datastore, Collection stores, Map map) {
        super(datastore, stores);
        this.nameToColumnIndex = map;
    }

    @Override
    public void publish(IStoreMessage message, List tuples) {
        List<Object[]> enrichedTuples = new ArrayList<Object[]>();
        if (( (Object[]) tuples.get(0))[nameToColumnIndex.get(HISTORY_STOCK_SYMBOL)].equals("SAN.PA"))
            System.out.println("dlwmeqv");

        int ind = nameToColumnIndex.get(HISTORY_CLOSE);
        Object[] currentTuple = null, nextTuple = null;
        int cpt = 0;
        while (cpt < tuples.size()) {
            if (nextTuple == null) {
                nextTuple = (Object[]) tuples.get(cpt);
            }
            else if (currentTuple == null) {
                currentTuple = nextTuple;
                nextTuple = (Object[]) tuples.get(cpt);
                if (currentTuple[ind] != null)
                    enrichedTuples.add(currentTuple);
            }
            else {
                currentTuple = nextTuple;
                nextTuple = (Object[]) tuples.get(cpt);

                if (currentTuple[ind] != null) {
                    if ((Double) currentTuple[ind] == 0.0)
                        currentTuple[ind] = ((Double) nextTuple[ind] +
                                (Double) enrichedTuples.get(enrichedTuples.size() - 1)[ind]) / 2;
                    enrichedTuples.add(currentTuple);
                }
            }
            cpt++;
        }

        Collection<Object[]> processedTuples = this.process(message, enrichedTuples);

        List storesList = (List) this.stores;

        this.datastore.getTransactionManager().addAll((java.lang.String) storesList.get(0),
                processedTuples);
    }
}


