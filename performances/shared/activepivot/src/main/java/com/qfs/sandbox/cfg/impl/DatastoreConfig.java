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
import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.INT;
import static com.qfs.literal.ILiteralType.LONG;
import static com.qfs.literal.ILiteralType.OBJECT;
import static com.qfs.literal.ILiteralType.STRING;

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


    /** Stock Price History Store */
    public static final String STOCK_PRICE_HISTORY_STORE_NAME = "StockPriceHistory";

    /** Sector Industry Company Store */
    public static final String SECTORS_INDUSTRY_COMPANY_STORE_NAME = "SectorsIndustryCompany";

    /** Portfolios store */
    public static final String PORTFOLIOS_STORE_NAME = "Portfolios";

    /** Indices store */

    // ////////////////////////////////////////////////
    // Fields
    // ////////////////////////////////////////////////

    // stock price history fields
    public static final String STOCK_PRICE_HISTORY__ID = "Id";
    public static final String STOCK_PRICE_HISTORY__DATE = "Date";
    public static final String STOCK_PRICE_HISTORY__OPEN = "Open";
    public static final String STOCK_PRICE_HISTORY__HIGH = "HighValue";
    public static final String STOCK_PRICE_HISTORY__LOW = "LowValue";
    public static final String STOCK_PRICE_HISTORY__CLOSE = "Close";
    public static final String STOCK_PRICE_HISTORY__VOLUME = "Volume";
    public static final String STOCK_PRICE_HISTORY__ADJ_CLOSE = "AdjClose";
    public static final String STOCK_PRICE_HISTORY__STOCK_SYMBOL = "StockSymbol";


    // SECTORS industry company fields
    public static final String SECTORS_INDUSTRY_COMPANY__ID = "Id";
    public static final String SECTORS_INDUSTRY_COMPANY__SECTOR = "Sector";
    public static final String SECTORS_INDUSTRY_COMPANY__INDUSTRY = "Industry";
    public static final String SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL = "StockSymbol";
    public static final String SECTORS_INDUSTRY_COMPANY__COMPANY_NAME = "CompanyName";

    // Portfolios fields
    public static final String PORTFOLIOS__ID = "Id";
    public static final String PORTFOLIOS__DATE = "Date";
    public static final String PORTFOLIOS__PORTFOLIO_TYPE = "PortfolioType";
    public static final String PORTFOLIOS__STOCK_SYMBOL = "StockSymbol";
    public static final String PORTFOLIOS__NUMBER_STOCKS = "NumberStocks";
    public static final String PORTFOLIOS__POSITION_TYPE = "PositionType";


    // ////////////////////////////////////////////////
    // Stores
    // ////////////////////////////////////////////////

    /** @return the description of the stock price history store */
    public IStoreDescription stockPriceHistoryStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(STOCK_PRICE_HISTORY_STORE_NAME)
//                .withField(STOCK_PRICE_HISTORY__ID, INT).asKeyField()
                .withField(STOCK_PRICE_HISTORY__DATE, "date[yyyy-MM-dd]").asKeyField()
                .withField(STOCK_PRICE_HISTORY__OPEN, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__HIGH, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__LOW, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__CLOSE, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__VOLUME, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__ADJ_CLOSE, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__STOCK_SYMBOL, STRING).asKeyField() //calculated column
                .updateOnlyIfDifferent()
                .build();
    }

    /** @return the description of the SECTORS/industry/company store */
    public IStoreDescription sectorsIndustryCompanyStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(SECTORS_INDUSTRY_COMPANY_STORE_NAME)
//                .withField(SECTORS_INDUSTRY_COMPANY__ID, INT).asKeyField()
                .withField(SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL, STRING).asKeyField()
                .withField(SECTORS_INDUSTRY_COMPANY__COMPANY_NAME, STRING).dictionarized() //creates a dictionnary which store only one time the string. Only for strings or dates
                .withField(SECTORS_INDUSTRY_COMPANY__SECTOR, STRING) //calculated column
                .withField(SECTORS_INDUSTRY_COMPANY__INDUSTRY, STRING) // calculated column
                .updateOnlyIfDifferent()
                .build();
    }


    /** @return the description of the portfolio store */
    public IStoreDescription portfoliosStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_STORE_NAME)
//                .withField(PORTFOLIOS__ID, INT).asKeyField()
                .withField(PORTFOLIOS__DATE, "date[yyyy-MM-dd]").asKeyField()
                .withField(PORTFOLIOS__PORTFOLIO_TYPE, STRING).asKeyField() // calculated column (custom or benchmark)
                .withField(PORTFOLIOS__NUMBER_STOCKS, DOUBLE)
                .withField(PORTFOLIOS__STOCK_SYMBOL, STRING).asKeyField()
                .withField(PORTFOLIOS__POSITION_TYPE, STRING)
                .updateOnlyIfDifferent()
                .build();
    }

    /**
     * Spring environment, automatically wired
     */
    @Autowired
    private Environment env;

    private static final Logger LOGGER = Logger.getLogger(DatastoreConfig.class.getSimpleName());



    @Bean
    /** @return the references between stores */
    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();

        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(STOCK_PRICE_HISTORY_STORE_NAME)
                .withName("PortfoliosToStockPriceHistory") // same as in cubeschema
                .withMapping(PORTFOLIOS__STOCK_SYMBOL, STOCK_PRICE_HISTORY__STOCK_SYMBOL)
                .withMapping(PORTFOLIOS__DATE, STOCK_PRICE_HISTORY__DATE)
                .build()
        );
        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(SECTORS_INDUSTRY_COMPANY_STORE_NAME)
                .withName("PortfoliosToSectors") // same as in cubeschema
                .withMapping(SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL, PORTFOLIOS__STOCK_SYMBOL)
                .build()
        );

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
        stores.add(stockPriceHistoryStore());
        stores.add(sectorsIndustryCompanyStore());
        stores.add(portfoliosStore());

        return new DatastoreSchemaDescription(stores, references());
    }
}
