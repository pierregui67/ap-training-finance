/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.context.impl;

import java.util.*;

import com.qfs.sandbox.bean.impl.DatastoreConfigBean;
import com.qfs.sandbox.context.ICurrencyContextValue;
import com.qfs.store.IDatastoreVersion;
import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.quartetfs.biz.pivot.context.ContextValueTranslationException;
import com.quartetfs.biz.pivot.context.IContextValueTranslator;
import com.quartetfs.biz.pivot.context.impl.SimpleContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.FOREX_INITIAL_CURRENCY;
import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.FOREX_STORE_NAME;
import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.FOREX_TARGET_CURRENCY;

/**
 *
 * Context value translator for reference currencies.
 * As a plugin value it is automatically registered
 * with the other available context value translators.
 *
 * @author Quartet FS
 *
 */

@QuartetPluginValue(intf=IContextValueTranslator.class)
public class CurrencyContextValueTranslator extends SimpleContextValueTranslator<String, ICurrencyContextValue> {

    /** serialVersionUID */
    private static final long serialVersionUID = -1129088664293462391L;

    public static HashSet<String> currencies = new HashSet<String>();

    /** Translator key */
    public static final String KEY = "referenceCurrency";

    public CurrencyContextValueTranslator() {
        super();

        IDatastoreVersion datastoreVersion = DatastoreConfigBean.getDatastoreConfig().datastore().getMostRecentVersion();
        Iterator it;

        // Get the foreign currencies
        it = DatastoreQueryHelper.selectDistinct(datastoreVersion, FOREX_STORE_NAME, FOREX_TARGET_CURRENCY).iterator();
        while (it.hasNext()) {
            currencies.add((String) it.next());
        }

        // Get the reference currency, the iterator should contain an single value.
        it = DatastoreQueryHelper.selectDistinct(datastoreVersion, FOREX_STORE_NAME, FOREX_INITIAL_CURRENCY).iterator();
        while (it.hasNext()) {
            currencies.add((String) it.next());
        }
        /*
        Alternative method : use the CurrenciesBean, initialize when creating the ForexStore.
        This solution is more elegant in this case but in order to keep an example of the
        DatastoreQueryHelper, one uses the DatastoreConfigBean.
         */
        // currencies = CurrenciesBean.getCurrencies();
    }

    @Override
    public Class<ICurrencyContextValue> getContextInterface() { return ICurrencyContextValue.class; }

    @Override
    public String key() { return KEY; }

    @Override
    protected ICurrencyContextValue createInstance(String content) {
        return new CurrencyContextValue(content);
    }


    @Override
    public Map<String, String> getAvailableProperties() {

        StringBuilder b = new StringBuilder();

        b.append("ENUM(");
        for (String currency : currencies) {
            b.append(currency).append(',');
        }
        b.deleteCharAt(b.length() - 1);
        b.append(')');


        return Collections.singletonMap(key(), b.toString());
    }


    @Override
    protected String format(String content) {
        return content;
    }

    @Override
    protected String parse(String property) throws ContextValueTranslationException {

        if (!currencies.contains(property))
            throw new ContextValueTranslationException("Currency not" +
                    " present in ENUM", key(), property, null);

        return property;
    }

    @Override
    protected String getContent(ICurrencyContextValue instance) {
        return instance.getCurrency();
    }

}
