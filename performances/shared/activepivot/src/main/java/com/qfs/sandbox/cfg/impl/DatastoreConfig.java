/*
 * (C) Quartet FS 2013-2014
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.desc.IDatastoreSchemaDescriptionPostProcessor;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.desc.impl.*;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.server.cfg.impl.ActivePivotConfig;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.INT;

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

    /*AP Spring configuration*/
    @Autowired
    protected ActivePivotConfig apConfig;

    //Datastores name
    public static final String PORTFOLIOS_STORE = "PortfolioStore";
    public static final String HISTORY_STORE = "HistoryStore";
    public static final String SECTORS_STORE = "SectorStore";
    public static final String CUSTOM_INDEX_DATA_STORE = "CustomIndexStore";
    public static final String FOREX_STORE = "ForexStore";

    //Fields name
    public static final String STOCK_SYMBOL = "StockSymbol";
    public static final String DATE = "Date";
    public static final String OPEN_PRICE = "OpenPrice";
    public static final String HIGH_PRICE = "HighPrice";
    public static final String LOW_PRICE = "LowPrice";
    public static final String CLOSE_PRICE = "ClosePrice";
    public static final String VOLUME = "Volume";
    public static final String ADJUSTED_CLOSED_PRICE = "AdjClosePrice";

    public static final String INDEX_NAME = "IndexName";
    public static final String COMPANY = "Company";
    public static final String STOCK_TYPE = "StockType";
    public static final String IDENTIFIER = "Identifier";

    public static final String SECTOR = "Sector";
    public static final String INDUSTRY = "Industry";

    public static final String PORTFOLIO_TYPE = "PortfolioType";
    public static final String QUANTITY = "Quantity";
    public static final String POSITION_TYPE = "PositionType";

    public static final String CURRENCY_PAIR = "CurrencyPair";
    public static final String RATE = "Rate";

    //BASE STORE
    /*DATASTORES*/

    @Bean
    public IStoreDescription portfolioStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_STORE)
                .withField(DATE,"date[yyyy-MM-dd]").asKeyField()
                .withField(PORTFOLIO_TYPE).asKeyField()//NAME
                .withField(QUANTITY, INT)
                .withField(STOCK_SYMBOL).asKeyField()
                .withField(POSITION_TYPE).dictionarized()
                .withModuloPartitioning(PORTFOLIO_TYPE,4)
                .build();
    }

    @Bean
    public IStoreDescription customIndexData() {
        return new StoreDescriptionBuilder()
                .withStoreName(CUSTOM_INDEX_DATA_STORE)
                .withField(STOCK_SYMBOL).asKeyField()
                .withField(COMPANY)
                .withField(CLOSE_PRICE, DOUBLE)
                .withField(IDENTIFIER)
                .build();
    }


    @Bean
    public IStoreDescription historyStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(HISTORY_STORE)
                .withField(DATE,"date[yyyy-MM-dd]").asKeyField()
                .withField(OPEN_PRICE, DOUBLE)
                .withField(HIGH_PRICE, DOUBLE)
                .withField(LOW_PRICE, DOUBLE)
                .withField(CLOSE_PRICE, DOUBLE)
                .withField(VOLUME, INT)
                .withField(ADJUSTED_CLOSED_PRICE, DOUBLE)
                .withField(STOCK_SYMBOL).asKeyField()
                .build();
    }

    @Bean
    public IStoreDescription sectorStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(SECTORS_STORE)
                .withField(STOCK_SYMBOL).asKeyField()
                .withField(COMPANY).dictionarized()
                .withField(SECTOR).dictionarized()
                .withField(INDUSTRY).dictionarized()
                .build();
    }

    @Bean
    public IStoreDescription forexStore() {
        return new StoreDescriptionBuilder()
                .withStoreName(FOREX_STORE)
                .withField(CURRENCY_PAIR).asKeyField()
                .withField(DATE, "date[yyyy-MM-dd]").asKeyField()
                .withField(RATE, DOUBLE)
                .withValuePartitioning(CURRENCY_PAIR) //WE ONLY HAVE 4 pairs
                .build();
    }


    /*REFERENCES*/
    @Bean
    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();

        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE).toStore(HISTORY_STORE)
                .withName(PORTFOLIOS_STORE + "To" + HISTORY_STORE)
                .withMapping(DATE,DATE).withMapping(STOCK_SYMBOL,STOCK_SYMBOL)
                .build());

        references.add(ReferenceDescription.builder()
                .fromStore(HISTORY_STORE).toStore(SECTORS_STORE)
                .withName(HISTORY_STORE + "To" + SECTORS_STORE)
                .withMapping(STOCK_SYMBOL,STOCK_SYMBOL)
                .build());

        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE).toStore(CUSTOM_INDEX_DATA_STORE)
                .withName(PORTFOLIOS_STORE + "To" + CUSTOM_INDEX_DATA_STORE)
                .withMapping(STOCK_SYMBOL,STOCK_SYMBOL)
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
                .addSchemaDescriptionPostProcessors(new DictionarizeStringsPostProcessor())
                .addSchemaDescriptionPostProcessors(new UpdateOnlyIfDifferentForReferencedStoresPostProcessor())
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
        stores.add(portfolioStore());
        stores.add(sectorStore());
        stores.add(historyStore());
        stores.add(customIndexData());
        stores.add(forexStore());
        return new DatastoreSchemaDescription(stores, references());
    }
}
