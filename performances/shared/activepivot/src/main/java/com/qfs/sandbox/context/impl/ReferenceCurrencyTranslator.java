package com.qfs.sandbox.context.impl;


import com.qfs.sandbox.bean.DatastoreConfigBean;
import com.qfs.sandbox.context.IReferenceCurrency;
import com.qfs.store.IDatastoreVersion;
import com.qfs.store.impl.Datastore;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.quartetfs.biz.pivot.context.ContextValueTranslationException;
import com.quartetfs.biz.pivot.context.IContextValueTranslator;
import com.quartetfs.biz.pivot.context.impl.SimpleContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX__TARGET_CURRENCY;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX__CURRENCY;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.FOREX_STORE_NAME;

@QuartetPluginValue(intf = IContextValueTranslator.class)
public class ReferenceCurrencyTranslator extends SimpleContextValueTranslator<String, IReferenceCurrency> {

    private static final long serialVersionUID = 201804271325L;

    public static HashSet<String> currencies = new HashSet<String>();

    public static final String KEY = "referenceCurrency";

    public ReferenceCurrencyTranslator(){
        super();

        IDatastoreVersion datastoreVersion = DatastoreConfigBean.getDatastoreConfig().datastore().getMostRecentVersion();
        Iterator it;

        it = DatastoreQueryHelper.selectDistinct(datastoreVersion, FOREX_STORE_NAME, FOREX__CURRENCY).iterator();
        while (it.hasNext()){
            currencies.add((String) it.next());
        }

        it = DatastoreQueryHelper.selectDistinct(datastoreVersion, FOREX_STORE_NAME, FOREX__TARGET_CURRENCY).iterator();
        while (it.hasNext()){
            currencies.add((String) it.next());
        }


    }

    @Override
    protected String format(String s) {
        return s;
    }

    @Override
    protected String parse(String s) throws ContextValueTranslationException {

        if (!currencies.contains(s))
            throw new ContextValueTranslationException("Currency not" +
                    " present in ENUM", key(), s, null);

        return s;
    }

    @Override
    protected IReferenceCurrency createInstance(String s) {
        return new ReferenceCurrency(s);
    }

    @Override
    protected String getContent(IReferenceCurrency iReferenceCurrency) {
        return iReferenceCurrency.getReferenceCurrency();
    }

    @Override
    public Class<IReferenceCurrency> getContextInterface() {
        return IReferenceCurrency.class;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public Map<String, String> getAvailableProperties() {
        StringBuilder builder = new StringBuilder();

        builder.append("ENUM(");
        for (String currency : currencies){
            builder.append(currency).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");

        return Collections.singletonMap(key(), builder.toString());
    }
}
