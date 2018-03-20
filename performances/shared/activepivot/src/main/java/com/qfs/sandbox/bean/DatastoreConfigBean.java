package com.qfs.sandbox.bean;

import com.qfs.server.cfg.IDatastoreConfig;
import org.springframework.stereotype.Component;

@Component
public class DatastoreConfigBean {

    private static IDatastoreConfig datastoreConfig;

    public static IDatastoreConfig getDatastoreConfig() {
        return datastoreConfig;
    }

    public void setDatastoreConfig(IDatastoreConfig datastoreConfig) {
        DatastoreConfigBean.datastoreConfig = datastoreConfig;
    }

}
