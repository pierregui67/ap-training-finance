/*
 * (C) Quartet FS 2013-2014
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.STRING;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.desc.impl.DatastoreSchemaDescription;
import com.qfs.desc.impl.ReferenceDescription;
import com.qfs.desc.impl.StoreDescriptionBuilder;
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

    private static final String SAMPLE_FIELD = "SampleField";
    /**
     * Spring environment, automatically wired
     */
    @Autowired
    private Environment env;

    private static final Logger LOGGER = Logger.getLogger(DatastoreConfig.class.getSimpleName());

    // ///////////////////////////////////////////////
    // Datastores definition :

    /** Name of the Stock Price History store */
    public static final String STOCK_PRICE_HISTORY_NAME = "StockPriceHistory";

    /** Name of the Portfolio store */
    public static final String PORTFOLIOS_NAME = "Portfolios";

    /** Name of the Company's informations store */
    public static final String COMPANY_INFORMATIONS_NAME = "Company";

    // Should not be defined here but in model.impl.Trade.java. Cf Sandbox.
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    // ///////////////////////////////////////////////
    // History store fields

    public static final String HISTORY_STOCK_SYMBOL = "StockSymbol";
    public static final String HISTORY_DATE = "Date";
    public static final String HISTORY_OPEN = "Open";
    public static final String HISTORY_HIGH = "High";
    public static final String HISTORY_LOW = "Low";
    public static final String HISTORY_CLOSE = "Close";
    public static final String HISTORY_VOLUME = "Volume";
    public static final String HISTORY_ADJ_CLOSE = "AdjClose";

    // ///////////////////////////////////////////////
    // Portfolio store fields

    public static final String PORTFOLIOS_DATE = "Date";
    public static final String PORTFOLIOS_TYPE = "Type";
    public static final String PORTFOLIOS_NUMBER_STOCKS = "NumberStocks";
    public static final String PORTFOLIOS_STOCK_SYMBOL = "StockSymbol";
    public static final String PORTFOLIOS_POSITION_TYPE = "PositionType";

    // ///////////////////////////////////////////////
    // Company Informations store fields

    public static final String COMPANY_STOCK_SYMBOL = "StockSymbol";
    public static final String COMPANY_NAME = "Name";
    public static final String COMPANY_SECTOR = "Sector";
    public static final String COMPANY_INDUSTRY = "Industry";

    // ////////////////////////////////////////////////
    // Stores

    /** @return the description of the stock price history store */
    @Bean
    public IStoreDescription historyStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(STOCK_PRICE_HISTORY_NAME)
                .withField(HISTORY_STOCK_SYMBOL, STRING).asKeyField()
                .withField(HISTORY_DATE, "date[" + DATE_PATTERN + "]").asKeyField()
                .withField(HISTORY_OPEN, DOUBLE)
                .withField(HISTORY_HIGH, DOUBLE)
                .withField(HISTORY_LOW, DOUBLE)
                .withField(HISTORY_CLOSE, DOUBLE)
                .withField(HISTORY_VOLUME,DOUBLE) // TODO : Is it really useful to use DOUBLE instead of INT ?
                .withField(HISTORY_ADJ_CLOSE, DOUBLE)
                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }

    /** @return the description of the portfolio store */
    @Bean
    public IStoreDescription portfolioStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_NAME)
                .withField(PORTFOLIOS_DATE, "date[" + DATE_PATTERN + "]").asKeyField()
                .withField(PORTFOLIOS_TYPE, STRING).asKeyField()
                .withField(PORTFOLIOS_NUMBER_STOCKS, DOUBLE) // TODO : Is it really useful to use DOUBLE instead of INT ?
                .withField(PORTFOLIOS_STOCK_SYMBOL, STRING).asKeyField()
                .withField(PORTFOLIOS_POSITION_TYPE, STRING)
                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }

    /** @return the description of the company informations store */
    @Bean
    public IStoreDescription companyInformationsStoreDescription() {
        return new StoreDescriptionBuilder()
                .withStoreName(COMPANY_INFORMATIONS_NAME)
                .withField(COMPANY_STOCK_SYMBOL, STRING).asKeyField()
                .withField(COMPANY_NAME, STRING)
                .withField(COMPANY_SECTOR, STRING)
                .withField(COMPANY_INDUSTRY, STRING)
                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }

    // ////////////////////////////////////////////////
    // References

    /** @return the references between stores */
    @Bean
    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_NAME)
                .toStore(STOCK_PRICE_HISTORY_NAME)
                .withName("PortfoliosToStockPriceHistory")
                .withMapping(PORTFOLIOS_STOCK_SYMBOL, HISTORY_STOCK_SYMBOL)
                .withMapping(PORTFOLIOS_DATE, HISTORY_DATE)
                .build());
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_NAME)
                .toStore(COMPANY_INFORMATIONS_NAME)
                .withName("PortfoliosToCompanyInformations")
                .withMapping(PORTFOLIOS_STOCK_SYMBOL, COMPANY_STOCK_SYMBOL)
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

        stores.add(historyStoreDescription());
        stores.add(portfolioStoreDescription());
        stores.add(companyInformationsStoreDescription());
        return new DatastoreSchemaDescription(stores, references());
    }
}
