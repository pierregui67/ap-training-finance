package com.qfs.sandbox.tuplepublisher.impl;

import com.qfs.sandbox.bean.impl.CurrenciesBean;
import com.qfs.source.IStoreMessage;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;

import java.util.*;

/**
 * This Tuple Publisher corrects the Forex data before publishing them in the store.
 * It consider the first currency (noted REF) of the first line as the reference currency.
 * Thus, every following rate will be expressed as : REF -> CUR : ...
 * The currency can be expressed in whatever order, with whatever links between currency with the
 * condition that they form a single currency tree.
 *
 * Warning :
 * 1)   If the user change the reference currency during the cube life time this will cause an error !
 *      Thus, if one wish to change the reference currency one will have to recompile the cube and
 *      change every place of the project where the reference currency was hard-coded.
 *      That is to say in where the PP are called i.e in Measures.xml. The query are carryied out
 *      by supposing that the reference currency is that one. If one has rebuild the ForexStore whith
 *      an other reference currency, the query will look for a record having as a key the depracated
 *      reference currency.
 * 2)   The currency relation without link with the reference currency will be ignored.
 * 3)   The Tuple Publisher does not manage the error case of a multiple currency definition
 */
public class ForexTuplePublisher extends TuplePublisher {

    public ForexTuplePublisher(IDatastore datastore, String store) {
        super(datastore, store);
    }

    @Override
    public void publish(IStoreMessage message, List tuples) {

        HashMap<Object, Object> currencyToRate = new HashMap<>();

        HashSet<String> foreignCurrency = new HashSet<>();
        int cpt = 0;

        Double rate;
        String pivotCurrency;

        List reorganizedTuples = new ArrayList();
        List unaffectedTuples = new ArrayList();
        String referenceCurrency = (String) ((Object[]) tuples.get(0))[0];
        while (cpt < tuples.size()) {
            Object[] currentTuple = (Object[]) tuples.get(cpt);
            // Inverse case
            if (currentTuple[0].equals(referenceCurrency)) {
                rate = (Double) currentTuple[2];
                pivotCurrency = (String) currentTuple[1];

                reorganizedTuples.add(new Object[]{referenceCurrency, pivotCurrency, rate});
                foreignCurrency.add(pivotCurrency);
                currencyToRate.put(pivotCurrency, rate);
            }
            else if (!currentTuple[0].equals(referenceCurrency) &&
                    currentTuple[1].equals(referenceCurrency)) {
                rate = 1.0 / (Double) currentTuple[2];
                pivotCurrency = (String) currentTuple[0];

                reorganizedTuples.add(new Object[]{referenceCurrency, pivotCurrency, rate});
                foreignCurrency.add(pivotCurrency);
                currencyToRate.put(pivotCurrency, rate);
            }
            else {
                unaffectedTuples.add(currentTuple);
            }
            cpt++;
        }

        // Now the non-regular forms
        boolean modif = true;
        while (!unaffectedTuples.isEmpty() && modif) {
            modif = false;
            Object[] currentTuple;
            Iterator it = unaffectedTuples.iterator();
            while (!modif && it.hasNext()) {
                currentTuple = (Object[]) it.next();
                // Triangulation case
                if (foreignCurrency.contains(currentTuple[0])) {
                    rate = (Double) currencyToRate.get(currentTuple[0])* (Double) currentTuple[2];
                    pivotCurrency = (String) currentTuple[1];
                    reorganizedTuples.add(new Object[]{referenceCurrency, pivotCurrency, rate});
                    foreignCurrency.add(pivotCurrency);
                    currencyToRate.put(pivotCurrency, rate);
                    unaffectedTuples.remove(currentTuple);
                    modif = true;
                }
                // Inverse triangulation case
                else if (foreignCurrency.contains(currentTuple[1])) {
                    rate = (Double) currencyToRate.get(currentTuple[1]) / (Double) currentTuple[2];
                    pivotCurrency = (String) currentTuple[0];
                    reorganizedTuples.add(new Object[]{referenceCurrency, pivotCurrency, rate});
                    foreignCurrency.add(pivotCurrency);
                    currencyToRate.put(pivotCurrency, rate);
                    unaffectedTuples.remove(currentTuple);
                    modif = true;
                }
            }
        }

        String storeName = (String) this.stores.toArray()[0];

        Collection<Object[]> processedTuples = this.process(message, reorganizedTuples);
        this.datastore.getTransactionManager().addAll(storeName, processedTuples);

        CurrenciesBean.addCurency(referenceCurrency);
        CurrenciesBean.addCurrency(foreignCurrency);
    }

}
