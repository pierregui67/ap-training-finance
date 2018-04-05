/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.gui.impl.JungSchemaPrinter;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.IWatcherService;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.csv.ICSVSourceConfiguration;
import com.qfs.msg.csv.IFileInfo;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.filesystem.impl.DirectoryCSVTopic;
import com.qfs.msg.csv.impl.CSVParserConfiguration;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.csv.translator.impl.AColumnCalculator;
import com.qfs.msg.impl.WatcherService;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.SchemaPrinter;
import com.qfs.store.transaction.ITransactionManager;
import com.qfs.util.timing.impl.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.List;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.STOCK_PRICE_HISTORY_STORE_NAME;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.SECTORS_INDUSTRY_COMPANY_STORE_NAME;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.PORTFOLIOS_STORE_NAME;


@PropertySource(value = { "classpath:directories.properties" })

/**
 * Spring configuration of the Sandbox ActivePivot server.<br>
 * The parameters of the Sandbox ActivePivot server can be quickly changed by modifying the
 * pojo.properties file.
 *
 * @author Quartet FS
 */
@Configuration
public class SourceConfig {

    @Autowired
    protected Environment env;

    @Autowired
    protected IDatastore datastore;



    public static final String STOCK_PRICE_HISTORY_TOPIC = STOCK_PRICE_HISTORY_STORE_NAME;
    public static final String SECTOR_TOPIC = SECTORS_INDUSTRY_COMPANY_STORE_NAME;
    public static final String PORTFOLIOS_TOPIC = PORTFOLIOS_STORE_NAME;
//    public static final String

    // CSV Load
    @Bean
    public IWatcherService watcherService() {
        return new WatcherService();
    }

    @Bean
    public CSVSource csvSource() {

        // defining the source
        CSVSource csvSource = new CSVSource();




        // /////////////////////////////////////
        // Add topics to your source

		DirectoryCSVTopic history = createDirectoryTopic(STOCK_PRICE_HISTORY_TOPIC, env.getProperty("dir.history"), 7, "**PriceHistory_*.csv", true);
		history.getParserConfiguration().setSeparator(',');
		csvSource.addTopic(history);

        DirectoryCSVTopic sectors = createDirectoryTopic(SECTOR_TOPIC, env.getProperty("dir.sectors"), 4, "**.csv", true);
        history.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(sectors);

        DirectoryCSVTopic portfolios = createDirectoryTopic(PORTFOLIOS_TOPIC, env.getProperty("dir.portfolios"), 7, "**.csv", false);
        history.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(portfolios);


        // ////////////////////////////////////////
        // Defining the source properties
        Properties sourceProps = new Properties();
        sourceProps.put(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, "4");
        csvSource.configure(sourceProps);
        return csvSource;
    }

    @Bean
    @DependsOn(value = "csvSource")
    public CSVMessageChannelFactory csvChannelFactory() {

        List<IColumnCalculator<ILineReader>> csvCalculatedColumns = new ArrayList<IColumnCalculator<ILineReader>>();

        csvCalculatedColumns.add(new AColumnCalculator<ILineReader>("StockSymbol") {
            @Override
            public Object compute(IColumnCalculationContext<ILineReader> iColumnCalculationContext) {
                String symbol = iColumnCalculationContext.getContext().getCurrentFile().getName();

                return symbol;
            }
        });

 		CSVMessageChannelFactory channelFactory = new CSVMessageChannelFactory(csvSource(), datastore);
 		channelFactory.setCalculatedColumns(STOCK_PRICE_HISTORY_TOPIC, STOCK_PRICE_HISTORY_STORE_NAME, csvCalculatedColumns);
        return channelFactory;
    }

    /**
     * Creating a directory topic
     *
     * @param topic       topic name
     * @param directory   relative directory (in the DATA dolder)
     * @param columnCount
     * @param pattern     pattern of each CSV file to be processed
     * @return
     */
    private DirectoryCSVTopic createDirectoryTopic(String topic, String directory, int columnCount, String pattern, boolean skipFirstLine) {
        CSVParserConfiguration cfg = new CSVParserConfiguration(columnCount);
        if (skipFirstLine) {
            cfg.setNumberSkippedLines(1);//skip the first line
        }
        String baseDir = env.getProperty("dir.base");
        return new DirectoryCSVTopic(topic, cfg, Paths.get(baseDir, directory), FileSystems.getDefault().getPathMatcher("glob:" + pattern), watcherService());
    }

    @Bean
    @DependsOn(value = "startManager")
    public Void initialLoad() throws Exception {
        //csv
        Collection<IMessageChannel<IFileInfo, ILineReader>> csvChannels = new ArrayList<>();
//		csvChannels.add(csvChannelFactory().createChannel(topic, datastore));

        long before = System.nanoTime();
        if (!Boolean.parseBoolean(env.getProperty("training.replay"))) {
            ITransactionManager transactionManager = datastore.getTransactionManager();
            transactionManager.startTransaction();
            //fetch the 2 sources and perform a bulk transaction
            csvSource().fetch(csvChannels);
            transactionManager.commitTransaction();
        } else {
            // read data files without sending anything to the datastore (that data is already loaded by the log replayer): those files won't then be considered as new files when enabling real time
        }

        long elapsed = System.nanoTime() - before; // log that somewhere
        printStoreSizes();

        return null;
    }

    private void printStoreSizes() {
        //add some logging
        if (Boolean.parseBoolean(env.getProperty("training.dev", "true"))) {
            //display the graph
            new JungSchemaPrinter(false).print("Training datastore", datastore);
        }

        // Print stop watch profiling
        StopWatch.get().printTimings();
        StopWatch.get().printTimingLegend();

        //print sizes
        SchemaPrinter.printStoresSizes(datastore.getMostRecentVersion().getSchema());
    }

}
