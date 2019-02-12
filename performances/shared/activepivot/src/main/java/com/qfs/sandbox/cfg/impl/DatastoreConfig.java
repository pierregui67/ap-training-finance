/*
 * (C) Quartet FS 2013-2014
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.desc.impl.DatastoreSchemaDescription;
import com.qfs.desc.impl.ReferenceDescription;
import com.qfs.desc.impl.StoreDescriptionBuilder;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.literal.impl.LiteralType;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.store.build.impl.DatastoreBuilder;
import com.qfs.store.IDatastore;
import com.qfs.store.log.ILogConfiguration;
import com.qfs.store.log.impl.LogConfiguration;
import com.qfs.store.log.ReplayException;
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

    // DataStores names

    /** Name of the history store */
    public static final String HISTORY_STORE_NAME = "History";

    /** Name of the index store */
    public static final String INDICES_STORE_NAME = "Indices";

    /** Name of the portfolios store */
    public static final String PORTFOLIOS_STORE_NAME = "Portfolios";

    /** Name of the sectors store */
    public static final String SECTORS_STORE_NAME = "Sectors";

    /** Name of the forex store */
    public static final String FOREX_STORE_NAME = "Forex";

    // ////////////////////////////////////////////////
    // Fields
    // ////////////////////////////////////////////////

    // ///////////////////////////////////////////////
    // History store fields

    public static final String HISTORY__DATE = "Date";
    public static final String HISTORY__OPEN = "Open";
    public static final String HISTORY__HIGH = "High";
    public static final String HISTORY__LOW = "Low";
    public static final String HISTORY__CLOSE = "Close";
    public static final String HISTORY__VOLUME = "Volume";
    public static final String HISTORY__ADJ_CLOSE = "AdjClose";
    public static final String HISTORY__STOCK_SYMB = "StockSymbol";

    // ///////////////////////////////////////////////
    // Sectors store fields

    public static final String SECTORS__STOCK_SYMB = "StockSymbol";
    public static final String SECTORS__COMPANY_NAME = "CompanyName";
    public static final String SECTORS__SECTOR = "Sector";
    public static final String SECTORS__INDUSTRY = "Industry";

    // ///////////////////////////////////////////////
    // Portfolios store fields

    public static final String PORTFOLIOS__DATE = "Date";
    public static final String PORTFOLIOS__PORTFOLIO_TYPE = "PortfolioType";
    public static final String PORTFOLIOS__NB_OF_STOCKS = "NbOfStocks";
    public static final String PORTFOLIOS__STOCK_SYMB = "StockSymbol";
    public static final String PORTFOLIOS__POSITION_TYPE = "PositionType";

    // ///////////////////////////////////////////////
    // Indices store fields

    public static final String INDICES__INDEX_NAME = "IndexName";
    public static final String INDICES__COMPANY_NAME = "CompanyName";
    public static final String INDICES__CLOSE_VALUE = "CloseValue";
    public static final String INDICES__STOCK_SYMB = "StockSymbol";
    public static final String INDICES__TIMESTAMP = "Timestamp";
    public static final String INDICES__EQUITY = "Equity";
    public static final String INDICES__DATE_TIME = "DateTime";
    public static final String INDICES__VOLUME = "Volume";

    // ///////////////////////////////////////////////
    // Forex store fields

    public static final String FOREX__CURRENCY_PAIR = "CurrencyPair";
    public static final String FOREX__DATE = "Date";
    public static final String FOREX__CLOSE_RATE = "CloseRate";

    /** Name of the reference from the portfolios store to the history store */
    public static final String PORTFOLIOS_TO_HISTORY_REF = "PortfoliosToHistory";

    /** Name of the reference from the portfolios store to the sectors store */
    public static final String PORTFOLIOS_TO_SECTORS_REF = "PortfoliosToSectors";

    /** Name of the reference from the portfolios store to the sectors store */
    public static final String PORTFOLIOS_TO_INDICES_REF = "PortfoliosToIndices";

    //Date pattern
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    // Define you datastores here:

    /** @return the description of the history store */
    @Bean
    public IStoreDescription historyStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(HISTORY_STORE_NAME)
                .withField(HISTORY__DATE, "date[" + DATE_PATTERN + "]").asKeyField()
                .withField(HISTORY__OPEN, LiteralType.DOUBLE)
                .withField(HISTORY__HIGH, LiteralType.DOUBLE)
                .withField(HISTORY__LOW, LiteralType.DOUBLE)
                .withField(HISTORY__CLOSE, LiteralType.DOUBLE)
                .withField(HISTORY__VOLUME, LiteralType.INT)
                .withField(HISTORY__ADJ_CLOSE, LiteralType.DOUBLE)
                .withField(HISTORY__STOCK_SYMB).asKeyField()
                .build();
    }

    /** @return the description of the sectors store */
    @Bean
    public IStoreDescription sectorsStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(SECTORS_STORE_NAME)
                .withField(SECTORS__STOCK_SYMB).asKeyField()
                .withField(SECTORS__COMPANY_NAME).dictionarized()
                .withField(SECTORS__SECTOR).dictionarized()
                .withField(SECTORS__INDUSTRY).dictionarized()
                .build();
    }

    /** @return the description of the portfolios store */
    @Bean
    public IStoreDescription portfoliosStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_STORE_NAME)
                .withField(PORTFOLIOS__DATE, "date[" + DATE_PATTERN + "]").asKeyField()
                .withField(PORTFOLIOS__PORTFOLIO_TYPE).asKeyField()
                .withField(PORTFOLIOS__NB_OF_STOCKS, LiteralType.INT)
                .withField(PORTFOLIOS__STOCK_SYMB).asKeyField()
                .withField(PORTFOLIOS__POSITION_TYPE).dictionarized()
                .withModuloPartitioning(PORTFOLIOS__PORTFOLIO_TYPE, 4)
                .build();
    }

    /** @return the description of the portfolios store */
    @Bean
    public IStoreDescription indicesStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(INDICES_STORE_NAME)
                .withField(INDICES__INDEX_NAME)
                .withField(INDICES__COMPANY_NAME)
                .withField(INDICES__CLOSE_VALUE)
                .withField(INDICES__STOCK_SYMB).asKeyField()
                .withField(INDICES__EQUITY)
                .withField(INDICES__DATE_TIME, "date[" + DATE_PATTERN + "]").asKeyField()
                .updateOnlyIfDifferent()
                .build();
    }

    /** @return the description of the forex store */
    @Bean
    public IStoreDescription forexStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(FOREX_STORE_NAME)
                .withField(FOREX__CURRENCY_PAIR).asKeyField()
                .withField(FOREX__DATE, "date[" + DATE_PATTERN + "]").asKeyField()
                .withField(FOREX__CLOSE_RATE, LiteralType.DOUBLE)
                .withValuePartitioning(FOREX__CURRENCY_PAIR)
                .build();
    }

    // Define your references here:
    @SuppressWarnings("Duplicates")
    @Bean
    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(HISTORY_STORE_NAME)
                .withName(PORTFOLIOS_TO_HISTORY_REF)
                .withMapping(PORTFOLIOS__STOCK_SYMB, HISTORY__STOCK_SYMB)
                .withMapping(PORTFOLIOS__DATE, HISTORY__DATE)
                .build());
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(SECTORS_STORE_NAME)
                .withName(PORTFOLIOS_TO_SECTORS_REF)
                .withMapping(PORTFOLIOS__STOCK_SYMB, SECTORS__STOCK_SYMB)
                .build());
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(INDICES_STORE_NAME)
                .withName(PORTFOLIOS_TO_INDICES_REF)
                .withMapping(PORTFOLIOS__STOCK_SYMB, INDICES__STOCK_SYMB)
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

//      Add all you stores here
        stores.add(historyStoreDescription());
        stores.add(sectorsStoreDescription());
        stores.add(portfoliosStoreDescription());
        stores.add(indicesStoreDescription());
        stores.add(forexStoreDescription());

        return new DatastoreSchemaDescription(stores, references());
    }
}
