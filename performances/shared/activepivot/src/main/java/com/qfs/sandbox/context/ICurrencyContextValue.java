package com.qfs.sandbox.context;

import com.quartetfs.biz.pivot.context.IContextValue;

public interface ICurrencyContextValue extends IContextValue{

    /** @return the currency */
    String getCurrency();
}