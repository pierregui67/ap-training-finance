package com.qfs.sandbox.context;

import com.quartetfs.biz.pivot.context.IContextValue;

public interface IReferenceCurrency  extends IContextValue{

    /** @return the reference currency */
    String getCurrency();

}
