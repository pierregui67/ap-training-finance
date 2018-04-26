package com.qfs.sandbox.cfg.impl;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.sandbox.datastore.impl.DatastoreDescriptionConfig;
import com.qfs.server.cfg.IActivePivotConfig;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.server.cfg.impl.ActivePivotManagerConfigFromContentServer;
import com.qfs.store.IDatastore;
import com.qfs.store.build.impl.DatastoreBuilder;
import com.qfs.store.log.ILogConfiguration;
import com.qfs.store.log.ReplayException;
import com.qfs.store.log.impl.LogConfiguration;
import com.qfs.store.transaction.IDatastoreWithReplay;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotDatastorePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Import(
        value = {
                DatastoreDescriptionConfig.class,
                //ActivePivotManagerConfigFromContentServer.class
        })
public class DatastoreConfig implements IDatastoreConfig {

    /**
     * Spring environment, automatically wired
     */
    @Autowired
    private Environment env;

    @Autowired
    protected IActivePivotConfig apConfig;

    @Autowired
    private IDatastoreSchemaDescription schemaDescription;

    private static final Logger LOGGER = Logger.getLogger(DatastoreConfig.class.getSimpleName());


    @Override
    @Bean
    public IDatastore datastore() {
        String logFolder = System.getProperty("user.home");
        ILogConfiguration logConfiguration = new LogConfiguration(logFolder);//the transaction logs will sit in your home directory, feel free to change the folder

        IDatastoreWithReplay dwr = new DatastoreBuilder()
                .setSchemaDescription(schemaDescription)
                .addSchemaDescriptionPostProcessors(ActivePivotDatastorePostProcessor.createFrom(apConfig.activePivotManagerDescription()))
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

}
