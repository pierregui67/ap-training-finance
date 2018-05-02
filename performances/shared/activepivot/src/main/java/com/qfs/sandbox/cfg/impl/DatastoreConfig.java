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
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.server.cfg.impl.ActivePivotConfig;
import com.qfs.store.IDatastore;
import com.qfs.store.build.impl.DatastoreBuilder;
import com.qfs.store.log.ILogConfiguration;
import com.qfs.store.log.ReplayException;
import com.qfs.store.log.impl.LogConfiguration;
import com.qfs.store.transaction.IDatastoreWithReplay;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotDatastorePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.INT;

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
    public static final String INDICES_STORE_NAME = "Indices";

    /** Forex store */
    public static final String FOREX_STORE_NAME = "Forex";

    @Autowired
    protected ActivePivotConfig apConfig;

    /** Indices store */

    // ////////////////////////////////////////////////
    // Fields
    // ////////////////////////////////////////////////

    // stock price history fields
    public static final String STOCK_PRICE_HISTORY__DATE = "Date";
    public static final String STOCK_PRICE_HISTORY__OPEN = "Open";
    public static final String STOCK_PRICE_HISTORY__HIGH = "HighValue";
    public static final String STOCK_PRICE_HISTORY__LOW = "LowValue";
    public static final String STOCK_PRICE_HISTORY__CLOSE = "Close";
    public static final String STOCK_PRICE_HISTORY__VOLUME = "Volume";
    public static final String STOCK_PRICE_HISTORY__ADJ_CLOSE = "AdjClose";
    public static final String STOCK_PRICE_HISTORY__STOCK_SYMBOL = "StockSymbol";


    // SECTORS industry company fields
    public static final String SECTORS_INDUSTRY_COMPANY__SECTOR = "Sector";
    public static final String SECTORS_INDUSTRY_COMPANY__INDUSTRY = "Industry";
    public static final String SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL = "StockSymbol";
    public static final String SECTORS_INDUSTRY_COMPANY__COMPANY_NAME = "CompanyName";

    // Portfolios fields
    public static final String PORTFOLIOS__DATE = "Date";
    public static final String PORTFOLIOS__PORTFOLIO_TYPE = "PortfolioType";
    public static final String PORTFOLIOS__STOCK_SYMBOL = "StockSymbol";
    public static final String PORTFOLIOS__NUMBER_STOCKS = "NumberStocks";
    public static final String PORTFOLIOS__POSITION_TYPE = "PositionType";


    // Indices fields
    public static final String INDICES__INDEX_NAME = "IndexName";
    public static final String INDICES__COMPANY_NAME = "CompanyName";
    public static final String INDICES__CLOSE_VALUE = "CloseValue";
    public static final String INDICES__STOCK_SYMBOL = "StockSymbol";
    public static final String INDICES__TIMESTAMP = "timestamp";
    public static final String INDICES__EQUITY = "Equity";
    public static final String INDICES__DATE_TIME = "Datetime";
    public static final String INDICES__VOLUME = "Volume";

    // Forex fields
    public static final String FOREX__CURRENCY = "Euro";
    public static final String FOREX__TARGET_CURRENCY = "Target currency";
    public static final String FOREX__RATIO = "Ratio";


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
                .withField(STOCK_PRICE_HISTORY__VOLUME, INT)
                .withField(STOCK_PRICE_HISTORY__ADJ_CLOSE, DOUBLE)
                .withField(STOCK_PRICE_HISTORY__STOCK_SYMBOL).asKeyField() //calculated column
//                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }

    /** @return the description of the SECTORS/industry/company store */
    public IStoreDescription sectorsIndustryCompanyStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(SECTORS_INDUSTRY_COMPANY_STORE_NAME)
//                .withField(SECTORS_INDUSTRY_COMPANY__ID, INT).asKeyField()
                .withField(SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL).asKeyField()
                .withField(SECTORS_INDUSTRY_COMPANY__COMPANY_NAME).dictionarized() //creates a dictionnary which store only one time the string. Only for strings or dates
                .withField(SECTORS_INDUSTRY_COMPANY__SECTOR)
                .withField(SECTORS_INDUSTRY_COMPANY__INDUSTRY)
//                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }


    /** @return the description of the portfolio store */
    public IStoreDescription portfoliosStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(PORTFOLIOS_STORE_NAME)
//                .withField(PORTFOLIOS__ID, INT).asKeyField()
                .withField(PORTFOLIOS__DATE, "date[yyyy-MM-dd]").asKeyField()
                .withField(PORTFOLIOS__PORTFOLIO_TYPE).asKeyField()
                .withField(PORTFOLIOS__NUMBER_STOCKS, INT)
                .withField(PORTFOLIOS__STOCK_SYMBOL).asKeyField()
                .withField(PORTFOLIOS__POSITION_TYPE)
//                .onDuplicateKeyWithinTransaction().logException()
                .updateOnlyIfDifferent()
                .build();
    }


    public IStoreDescription indicesStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(INDICES_STORE_NAME)
                .withField(INDICES__INDEX_NAME)
                .withField(INDICES__COMPANY_NAME)
                .withField(INDICES__CLOSE_VALUE)
                .withField(INDICES__STOCK_SYMBOL).asKeyField()
//                .withField(INDICES__TIMESTAMP)
                .withField(INDICES__EQUITY)
                .withField(INDICES__DATE_TIME, "date[yyyy-MM-dd]").asKeyField()
//                .withField(INDICES__VOLUME)
                .updateOnlyIfDifferent()
                .build();
    }

    public IStoreDescription forexStore(){
        return new StoreDescriptionBuilder()
                .withStoreName(FOREX_STORE_NAME)
                .withField(FOREX__CURRENCY)
                .withField(FOREX__TARGET_CURRENCY).asKeyField()
                .withField(FOREX__RATIO, DOUBLE)
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
                .dontIndexOwner()// don't index parents, when there's no update in child
                .build()
        );

        references.add(ReferenceDescription.builder()
                .fromStore(PORTFOLIOS_STORE_NAME)
                .toStore(INDICES_STORE_NAME)
                .withName("PortfoliosToIndices")
                .withMapping(PORTFOLIOS__STOCK_SYMBOL, INDICES__STOCK_SYMBOL)
                .build()

        );


        return references;
    }

    @Override
    @Bean
    public IDatastore datastore() {
        String logFolder = System.getProperty("user.home");
        ILogConfiguration logConfiguration = new LogConfiguration(logFolder);//the transaction logs will sit in your home directory, feel free to change the folder


        // dictionnarize all the fields
        ActivePivotDatastorePostProcessor schemaPostProcessor = ActivePivotDatastorePostProcessor.createFrom(apConfig.activePivotManagerDescription());
        IDatastoreWithReplay dwr = new DatastoreBuilder()
                .setSchemaDescription(datastoreSchemaDescription())
                .addSchemaDescriptionPostProcessors(schemaPostProcessor) // dictionarize all the files https://support.quartetfs.com/confluence/display/AP5/Datastore+Builder
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
        stores.add(indicesStore());
        stores.add(forexStore());

        return new DatastoreSchemaDescription(stores, references());
    }
}
