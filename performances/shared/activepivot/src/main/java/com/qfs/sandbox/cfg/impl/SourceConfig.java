/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.condition.impl.BaseConditions;
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
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.SchemaPrinter;
import com.qfs.store.query.ICursor;
import com.qfs.store.query.condition.impl.RecordQuery;
import com.qfs.store.query.impl.DatastoreQueryHelper;
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

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;
import static org.jgroups.util.Util.assertEquals;

@PropertySource(value = { "classpath:perf.properties" })

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

    public static final char COMMA_SEPARATOR = ',';
    public static final char BAR_SEPARATOR = '|';

    public static final String STOCK_PRICE_HISTORY_TOPIC = STOCK_PRICE_HISTORY_NAME;
    public static final String COMPAGNY_INFORMATIONS_TOPIC = COMPANY_INFORMATIONS_NAME;
    public static final String PORTFOLIOS_TOPIC = PORTFOLIOS_NAME;

    // CSV Load
    @Bean
    public IWatcherService watcherService() {
        return new WatcherService();
    }

    @Bean
    public CSVSource csvSource() {
        CSVSource csvSource = new CSVSource();

        // ////////////////////////////////////////////////
        // Create Maps
        Map <Integer, String> mapPortfolios = new HashMap<Integer, String>();
        mapPortfolios.put(0, PORTFOLIOS_DATE);
        mapPortfolios.put(1, PORTFOLIOS_TYPE);
        mapPortfolios.put(2, PORTFOLIOS_NUMBER_STOCKS);
        mapPortfolios.put(3, PORTFOLIOS_STOCK_SYMBOL);
        mapPortfolios.put(4, PORTFOLIOS_POSITION_TYPE);

        Map <Integer, String> mapHistory = new HashMap<Integer, String>();
        mapHistory.put(0, HISTORY_DATE);
        mapHistory.put(1, HISTORY_OPEN);
        mapHistory.put(2, HISTORY_HIGH);
        mapHistory.put(3, HISTORY_LOW);
        mapHistory.put(4, HISTORY_CLOSE);
        mapHistory.put(5, HISTORY_VOLUME);
        mapHistory.put(6, HISTORY_ADJ_CLOSE);

        Map <Integer, String> mapCompany = new HashMap<Integer, String>();
        mapCompany.put(0, COMPANY_STOCK_SYMBOL);
        mapCompany.put(1, COMPANY_NAME);
        mapCompany.put(2, COMPANY_SECTOR);
        mapCompany.put(3, COMPANY_INDUSTRY);



        // ////////////////////////////////////////////////
        // Add topics
        DirectoryCSVTopic portfolios = createDirectoryTopic(PORTFOLIOS_TOPIC, env.getProperty("dir.portfolios"), 6, "**Initial**.csv", false, mapPortfolios);
        portfolios.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(portfolios);

        DirectoryCSVTopic history = createDirectoryTopic(STOCK_PRICE_HISTORY_TOPIC, env.getProperty("dir.history"), 7, "**PriceHistory_*.csv", true, mapHistory);
        history.getParserConfiguration().setSeparator(COMMA_SEPARATOR);
        csvSource.addTopic(history);

        DirectoryCSVTopic company = createDirectoryTopic(COMPAGNY_INFORMATIONS_TOPIC, env.getProperty("dir.company"), 4, "**.csv", true, mapCompany);
        company.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(company);

        // TODO : what are those properties ?
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
                                         symbol = symbol.replace("PriceHistory_","");
                                         symbol = symbol.replace(".csv","");
                                         symbol = symbol.replace("-", ".");
                                         return symbol;
                                     }
                                 });

        CSVMessageChannelFactory channelFactory = new CSVMessageChannelFactory(csvSource(), datastore);
 		channelFactory.setCalculatedColumns(STOCK_PRICE_HISTORY_TOPIC, STOCK_PRICE_HISTORY_NAME, csvCalculatedColumns);
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
    private DirectoryCSVTopic createDirectoryTopic(String topic, String directory, int columnCount, String pattern, boolean skipFirstLine, Map<Integer, String> mapIndexToField) {
        CSVParserConfiguration cfg = new CSVParserConfiguration(columnCount);
        cfg.setColumns(mapIndexToField);
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
        csvChannels.add(csvChannelFactory().createChannel(PORTFOLIOS_TOPIC));
		csvChannels.add(csvChannelFactory().createChannel(STOCK_PRICE_HISTORY_TOPIC));
        csvChannels.add(csvChannelFactory().createChannel(COMPAGNY_INFORMATIONS_TOPIC));


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
        /*datastore.getTransactionManager().
        // check that the data was successfully loaded into the datastore
        ICursor cursor = datastore.getLatestVersion().execute(
                new RecordQuery("StockPriceHistory", BaseConditions.TRUE, Arrays.asList("StockSymbol", "Date", "Open", "High", "Low", "Close", "Volume", "AdjClose")));
        assertEquals(8, DatastoreQueryHelper.getCursorSize(cursor));*/

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
