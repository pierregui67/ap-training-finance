package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

import java.util.Objects;

final public class CurrencyContextValue extends AContextValue implements  ICurrencyContextValue {

    private static final long serialVersionUID = 4214287933898300666L;
    private String currency;

    private CurrencyContextValue(){
        this.currency = "EUR";
    }
    public CurrencyContextValue(String currency){
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    private void setCurrency(String newCurrency) {
        currency = newCurrency;
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
