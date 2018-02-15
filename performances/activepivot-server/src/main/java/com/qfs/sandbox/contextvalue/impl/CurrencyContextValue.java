package com.qfs.sandbox.contextvalue.impl;

import com.qfs.sandbox.contextvalue.ICurrencyContextValue;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

import java.util.Objects;

public final class CurrencyContextValue extends AContextValue implements ICurrencyContextValue {

    private String currency;

    private CurrencyContextValue(){}

    public CurrencyContextValue(String currency) {
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    private void setCurrency(String currency) {
        // required for JAXB unmarshalling
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyContextValue that = (CurrencyContextValue) o;
        return Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency);
    }

    @Override
    public Class<? extends IContextValue> getContextInterface() {
        return ICurrencyContextValue.class;
    }
}
