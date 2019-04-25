/*
 * (C) Quartet FS 2013-2014
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.desc.impl.DatastoreSchemaDescription;
import com.qfs.desc.impl.ReferenceDescription;
import com.qfs.desc.impl.StoreDescriptionBuilder;
import com.qfs.literal.ILiteralType;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.store.IDatastore;
import com.qfs.store.build.impl.DatastoreBuilder;
import com.qfs.store.log.ILogConfiguration;
import com.qfs.store.log.ReplayException;
import com.qfs.store.log.impl.LogConfiguration;
import com.qfs.store.transaction.IDatastoreWithReplay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring configuration of the Datastore.
 *
 * @author Quartet FS
 */


@Configuration
public class DatastoreConfig implements IDatastoreConfig {

    /**
     * Spring environment, automatically wired
     */

    @Autowired
    private Environment env;

    private static final Logger LOGGER = Logger.getLogger(DatastoreConfig.class.getSimpleName());

    //Store names
    private static final String HISTORY_STORE_NAME = "HistoryStore";
    private static final String SECTOR_STORE_NAME = "SectorStore";
    private static final String PORTFOLIOS_STORE_NAME ="PortfoliosStore";
    private static final String INDEX_STORE_NAME = "IndexStore";


    //History Store fields
    private static final String HISTORY_STOCK_SYMBOL = "HistoryStockSymbol";
    private static final String HISTORY_DATE = "HistoryDate";
    private static final String OPEN = "Open";
    private static final String HIGH = "High";
    private static final String LOW = "Low";
    private static final String CLOSE = "Close";
    private static final String VOLUME = "Volume";
    private static final String ADJ_CLOSE = "AdjustedClose";

    //Sector Store fields
    private static final String SECTOR_STOCK_SYMBOL = "SectorStockSymbol";
    private static final String COMPANY_NAME = "CompanyName";
    private static final String SECTOR = "Sector";
    private static final String INDUSTRY = "Industry";

    //Portfolios Store fields
    private static final String PORTFOLIOS_STOCK_SYMBOL = "PortfoliosStockSymbol";
    private static final String PORTFOLIOS_TYPE = "PortfoliosType";
    private static final String STOCK_NUMBER = "StockNumber";
    private static final String POSITION_TYPE = "PositionType";
    private static final String DATE = "Date";

    //Index Store fields
    private static final String INDEX_STOCK_SYMBOL = "IndexStockSymbol";
    private static final String INDEX_COMPANY_NAME = "IndexCompanyName";
    private static final String INDEX_STOCK_VALUE = "IndexStockValue";
    private static final String INDEX_NAME = "IndexName";
    private static final String INDEX_DATE = "IndexDate";
    private static final String INDEX_POSITION_TYPE = "PositionType";
    private static final String INDEX_ID = "IndexId";
    private static final String INDEX_NUMBER = "IndexNumber";
    //Reference names list
    private static final String BASE_TO_HISTORY_REF = "BaseToHistory";
    private static final String BASE_TO_SECTOR = "BaseToSector";
    private static final String BASE_TO_INDEX = "BaseToIndex";



    // DataStore List :


    public IStoreDescription sectorStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(SECTOR_STORE_NAME)
                .withField(SECTOR_STOCK_SYMBOL).asKeyField()
                .withField(COMPANY_NAME).dictionarized()
                .withField(SECTOR).dictionarized()
                .withField(INDUSTRY).dictionarized()
                .build();
    }

    public IStoreDescription portfoliosStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_STORE_NAME)
                .withField(DATE, DATE +"[yyyy-MM-dd]").asKeyField()
                .withField(PORTFOLIOS_TYPE).dictionarized().asKeyField()
                .withField(STOCK_NUMBER,    ILiteralType.INT)
                .withField(PORTFOLIOS_STOCK_SYMBOL).asKeyField()
                .withField(POSITION_TYPE)
                .updateOnlyIfDifferent()
                .build();
    }

    public IStoreDescription historyStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(HISTORY_STORE_NAME)
                .withField(HISTORY_DATE,    DATE +"[yyyy-MM-dd]").asKeyField()
                .withField(OPEN,            ILiteralType.DOUBLE)
                .withField(HIGH,            ILiteralType.DOUBLE)
                .withField(LOW,             ILiteralType.DOUBLE)
                .withField(CLOSE,           ILiteralType.DOUBLE)
                .withField(VOLUME,          ILiteralType.DOUBLE)
                .withField(ADJ_CLOSE,       ILiteralType.DOUBLE)
                .withField(HISTORY_STOCK_SYMBOL).asKeyField()
                .build();
    }

    public IStoreDescription indexStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(INDEX_STORE_NAME)
                .withField(INDEX_NAME).dictionarized()
                .withField(INDEX_COMPANY_NAME).dictionarized()
                .withField(INDEX_STOCK_VALUE,   ILiteralType.DOUBLE)
                .withField(INDEX_STOCK_SYMBOL).asKeyField()
                .withField(INDEX_ID,            ILiteralType.INT)
                .withField(INDEX_POSITION_TYPE).dictionarized()
                .withField(INDEX_DATE,          DATE + "[yyyy-MM-dd]").asKeyField()
                .withField(INDEX_NUMBER,        ILiteralType.INT)
                .build();

    }


    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();

        //Reference list
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(HISTORY_STORE_NAME)
                .withName(BASE_TO_HISTORY_REF)
                .withMapping(PORTFOLIOS_STOCK_SYMBOL, HISTORY_STOCK_SYMBOL)
                .withMapping(DATE,HISTORY_DATE)
                .build());
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(SECTOR_STORE_NAME)
                .withName(BASE_TO_SECTOR)
                .withMapping(PORTFOLIOS_STOCK_SYMBOL, SECTOR_STOCK_SYMBOL)
                .build());

        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(INDEX_STORE_NAME)
                .withName(BASE_TO_INDEX)
                .withMapping(PORTFOLIOS_STOCK_SYMBOL, INDEX_STOCK_SYMBOL)
                .withMapping(DATE, INDEX_DATE)
                .build());

        return references;
    }

    @Override
    @Bean
    public IDatastore datastore() {
        String logFolder = System.getProperty("user.home");
        ILogConfiguration logConfiguration = new LogConfiguration(logFolder);//the transaction logs will sit in your home directory, feel free to change the folder
        IDatastoreWithReplay dwr = new DatastoreBuilder()
                .setSchemaDescription(datastoreSchemaDescription())
                .setLogConfiguration(logConfiguration)
                .withReplay()
                .build();

        if (Boolean.parseBoolean(env.getProperty("training.replay"))) {
            LOGGER.log(Level.INFO, String.format("********************* Replaying the transaction log located under [%s] *********************", logFolder));
            try {
                return dwr.replay();
            } catch (ReplayException e) {
                LOGGER.log(Level.SEVERE, "Error while replaying transactions, will skip and delete the replay file");
            }
        }
        return dwr.skipAndDeleteReplay();
    }

    /**
     * Provide the schema description of the datastore.
     * <p>
     * It is based on the descriptions of the store
     * <p>
     * s in
     * the datastore, the descriptions of the references
     * between those stores, and the optimizations and
     * constraints set on the schema.
     *
     * @return schema description
     */

    @Bean
    public IDatastoreSchemaDescription datastoreSchemaDescription() {
        final Collection<IStoreDescription> stores = new LinkedList<>();

        //Store List
        stores.add(portfoliosStore());
        stores.add(sectorStore());
        stores.add(historyStore());
        stores.add(indexStore());
        return new DatastoreSchemaDescription(stores, references());
    }
}
