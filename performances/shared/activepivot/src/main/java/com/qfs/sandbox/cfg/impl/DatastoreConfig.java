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

    private static final String SAMPLE_FIELD = "SampleField";
    /**
     * Spring environment, automatically wired
     */
    @Autowired
    private Environment env;

    private static final Logger LOGGER = Logger.getLogger(DatastoreConfig.class.getSimpleName());
    // Define you datastores here:

    @Bean
    public IStoreDescription HistoryDataStore() {
        return new StoreDescriptionBuilder().withStoreName("History")
                .withField("Date", "date[yyyy-MM-dd]").asKeyField()
                .withField("Open", ILiteralType.DOUBLE)
                .withField("High", ILiteralType.DOUBLE)
                .withField("Low", ILiteralType.DOUBLE)
                .withField("Close", ILiteralType.DOUBLE).dictionarized()
                .withField("Volume", ILiteralType.DOUBLE)
                .withField("Adj Close", ILiteralType.DOUBLE)
                .withField("Stock Symbol", ILiteralType.STRING).dictionarized().asKeyField()
                .updateOnlyIfDifferent()
                .build();
    }
    @Bean
    public IStoreDescription IndicesDataStore() {
        return new StoreDescriptionBuilder().withStoreName("Indices")
                .withField("Index Name", ILiteralType.STRING).dictionarized()
                .withField("Counterparty", ILiteralType.STRING).dictionarized()
                .withField("Num", ILiteralType.DOUBLE)
                .withField("Stock Symbol", ILiteralType.STRING).dictionarized().asKeyField()
                .withField("Quantity", ILiteralType.INT)
                .withField("Trade Type", ILiteralType.STRING).dictionarized()
                .withField("Date",  "date[yyyy-MM-dd]")
                .withField("Value", ILiteralType.INT)
                .updateOnlyIfDifferent()
                .build();
    }
    @Bean
    public IStoreDescription SectorDataStore() {
        return new StoreDescriptionBuilder().withStoreName("Sector")
                .withField("Stock Symbol", ILiteralType.STRING).dictionarized().asKeyField()
                .withField("Compagny Name", ILiteralType.STRING).dictionarized()
                .withField("Sector", ILiteralType.STRING).dictionarized()
                .withField("Industry", ILiteralType.STRING).dictionarized()
                .updateOnlyIfDifferent()
                .build();
    }
    @Bean
    public IStoreDescription PortfoliosDataStore() {
        return new StoreDescriptionBuilder().withStoreName("Portfolios")
                .withField("Date",  "date[yyyy-MM-dd]").asKeyField()
                .withField("Portfolio type", ILiteralType.STRING).dictionarized().asKeyField()
                .withField("Number of stocks", ILiteralType.INT)
                .withField("Stock Symbol", ILiteralType.STRING).dictionarized().asKeyField()
                .withField("Position Type", ILiteralType.STRING).dictionarized()
                .withPartitioning("hash8(Date)")
                .updateOnlyIfDifferent()
                .build();
    }

    // Define your references here:

    @Bean
    public Collection<IReferenceDescription> references() {
        final Collection<IReferenceDescription> references = new LinkedList<>();

        references.add(ReferenceDescription.builder()
                .fromStore("Portfolios")
                .toStore("Indices")
                .withName("PortfoliosToIndices")
                .withMapping("Stock Symbol", "Stock Symbol")
                .build());

        references.add(ReferenceDescription.builder()
                .fromStore("Portfolios")
                .toStore("Sector")
                .withName("PortfoliosToSector")
                .withMapping("Stock Symbol", "Stock Symbol")
                .build());

        references.add(ReferenceDescription.builder()
                .fromStore("Portfolios")
                .toStore("History")
                .withName("PortfoliosToHistory")
                .withMapping("Date", "Date")
                .withMapping("Stock Symbol", "Stock Symbol")
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

        stores.add(HistoryDataStore());
        stores.add(IndicesDataStore());
        stores.add(SectorDataStore());
        stores.add(PortfoliosDataStore());

        return new DatastoreSchemaDescription(stores, references());
    }
}
