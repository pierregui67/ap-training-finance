/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

import com.qfs.gui.impl.JungSchemaPrinter;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.IWatcherService;
import com.qfs.msg.csv.ICSVSourceConfiguration;
import com.qfs.msg.csv.IFileInfo;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.filesystem.impl.DirectoryCSVTopic;
import com.qfs.msg.csv.impl.CSVParserConfiguration;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.csv.translator.impl.AColumnCalculator;
import com.qfs.msg.impl.WatcherService;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.store.IDatastore;
import com.qfs.store.IDatastoreSchemaMetadata;
import com.qfs.store.impl.SchemaPrinter;
import com.qfs.store.impl.StoreUtils;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Spring configuration of the Sandbox ActivePivot server.<br>
 * The parameters of the Sandbox ActivePivot server can be quickly changed by modifying the
 * pojo.properties file.
 *
 * @author Quartet FS
 */
@PropertySource(value = {"classpath:directories.properties"})
@Configuration
public class SourceConfig {

    @Autowired
    protected Environment env;

    @Autowired
    protected IDatastore datastore;

    // Topics Name
    public static final String HISTORY_TOPIC = HISTORY_STORE_NAME;
    public static final String PORTFOLIOS_TOPIC = PORTFOLIOS_STORE_NAME;
    public static final String SECTORS_TOPIC = SECTORS_STORE_NAME;


    // CSV Load
    @Bean
    public IWatcherService watcherService() {
        return new WatcherService();
    }

    @SuppressWarnings("Duplicates")
    @Bean
    public CSVSource csvSource() {
        CSVSource csvSource = new CSVSource();

        // History Mapping
        Map<Integer,String> historyMapping = new HashMap<>();
        historyMapping.put(0, HISTORY__DATE);
        historyMapping.put(1, HISTORY__OPEN);
        historyMapping.put(2, HISTORY__HIGH);
        historyMapping.put(3, HISTORY__LOW);
        historyMapping.put(4, HISTORY__CLOSE);
        historyMapping.put(5, HISTORY__VOLUME);
        historyMapping.put(6, HISTORY__ADJ_CLOSE);

        // Portfolios Mapping
        Map<Integer, String> portfoliosMapping = new HashMap<>();
        portfoliosMapping.put(0, PORTFOLIOS__DATE);
        portfoliosMapping.put(1, PORTFOLIOS__PORTFOLIO_TYPE);
        portfoliosMapping.put(2, PORTFOLIOS__NB_OF_STOCKS);
        portfoliosMapping.put(3, PORTFOLIOS__STOCK_SYMB);
        portfoliosMapping.put(4, PORTFOLIOS__POSITION_TYPE);

        // Sectors Mapping
        Map<Integer, String> sectorsMapping = new HashMap<>();
        sectorsMapping.put(0, SECTORS__STOCK_SYMB);
        sectorsMapping.put(1, SECTORS__COMPANY_NAME);
        sectorsMapping.put(2, SECTORS__SECTOR);
        sectorsMapping.put(3, SECTORS__INDUSTRY);

        // Add topics here, eg.
		DirectoryCSVTopic history = createDirectoryTopic(HISTORY_TOPIC, env.getProperty("dir.history"), 7, "**PriceHistory_*.csv", true, historyMapping);
		history.getParserConfiguration().setSeparator(',');
		csvSource.addTopic(history);

        DirectoryCSVTopic sectors = createDirectoryTopic(SECTORS_TOPIC, env.getProperty("dir.sectors"), 4, "**.csv", true, sectorsMapping);
        sectors.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(sectors);

        DirectoryCSVTopic portfolios = createDirectoryTopic(PORTFOLIOS_TOPIC, env.getProperty("dir.portfolios"), 6, "**.csv", false, portfoliosMapping);
        portfolios.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(portfolios);

        Properties sourceProps = new Properties();
        sourceProps.put(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, "4");
        csvSource.configure(sourceProps);
        return csvSource;
    }

    @Bean
    @DependsOn(value = "csvSource")
    public CSVMessageChannelFactory csvChannelFactory() {

        List<IColumnCalculator<ILineReader>> csvCalculatedColumnsPortfolio = new ArrayList<>();
        csvCalculatedColumnsPortfolio.add(new AColumnCalculator<ILineReader>(HISTORY__STOCK_SYMB) {
            @Override
            public Object compute(IColumnCalculationContext<ILineReader> iColumnCalculationContext) {
                String fileName = iColumnCalculationContext.getContext().getCurrentFile().getName();
                return fileName.replace("PriceHistory_", "")
                                .replace(".csv", "")
                                .replace("-", ".");
            }
        });
        // Add calculated columns here
        CSVMessageChannelFactory channelFactory = new CSVMessageChannelFactory(csvSource(), datastore);
 		channelFactory.setCalculatedColumns(HISTORY_TOPIC, HISTORY_STORE_NAME, csvCalculatedColumnsPortfolio);

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

    private DirectoryCSVTopic createDirectoryTopic(String topic, String directory, int columnCount, String pattern, boolean skipFirstLine, Map<Integer,String> fieldsMapping) {
        CSVParserConfiguration cfg = new CSVParserConfiguration(columnCount);
        cfg.setColumns(fieldsMapping);
        if (skipFirstLine) {
            cfg.setNumberSkippedLines(1);//skip the first line
        }
        String baseDir = env.getProperty("dir.data");
        return new DirectoryCSVTopic(topic, cfg, Paths.get(baseDir, directory), FileSystems.getDefault().getPathMatcher("glob:" + pattern), watcherService());
    }

    @Bean
    @DependsOn(value = "startManager")
    public Void initialLoad() throws Exception {
        //csv
        Collection<IMessageChannel<IFileInfo, ILineReader>> csvChannels = new ArrayList<>();
		csvChannels.add(csvChannelFactory().createChannel(HISTORY_TOPIC));
		csvChannels.add(csvChannelFactory().createChannel(PORTFOLIOS_TOPIC));
		csvChannels.add(csvChannelFactory().createChannel(SECTORS_TOPIC));

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
