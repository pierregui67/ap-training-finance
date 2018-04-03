/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.gui.impl.JungSchemaPrinter;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.IWatcherService;
import com.qfs.msg.csv.ICSVSourceConfiguration;
import com.qfs.msg.csv.IFileInfo;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.filesystem.impl.DirectoryCSVTopic;
import com.qfs.msg.csv.impl.CSVColumnParser;
import com.qfs.msg.csv.impl.CSVParserConfiguration;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.csv.translator.impl.AColumnCalculator;
import com.qfs.msg.impl.WatcherService;
import com.qfs.sandbox.tuplepublisher.impl.ForexTuplePublisher;
import com.qfs.sandbox.tuplepublisher.impl.IndicesTuplePublisher;
import com.qfs.source.ITuplePublisher;
import com.qfs.source.impl.AutoCommitTuplePublisher;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.SchemaPrinter;
import com.qfs.store.log.impl.LogWriteException;
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

    public static final String STOCK_PRICE_HISTORY_TOPIC = STOCK_PRICE_HISTORY_STORE_NAME;
    public static final String COMPAGNY_INFORMATIONS_TOPIC = COMPANY_INFORMATIONS_STORE_NAME;
    public static final String PORTFOLIOS_TOPIC = PORTFOLIOS_STORE_NAME;
    public static final String INDICES_TOPIC = INDICES_STORE_NAME;
    public static final String FOREX_TOPIC = FOREX_STORE_NAME;
    public static final String INDICES_HISTORY_TOPIC = "IndicesHistory";

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
        Map<Integer, String> mapPortfolios = new HashMap<Integer, String>();
        Map <Integer, String> mapHistory = new HashMap<Integer, String>();
        Map <Integer, String> mapCompany = new HashMap<Integer, String>();
        Map <Integer, String> mapIndices = new HashMap<Integer, String>();
        Map <Integer, String> mapForex = new HashMap<Integer, String>();

        mapPortfolios.put(0, PORTFOLIOS_DATE);
        mapPortfolios.put(1, PORTFOLIOS_INDEX_NAME);
        mapPortfolios.put(2, PORTFOLIOS_NUMBER_STOCKS);
        mapPortfolios.put(3, PORTFOLIOS_STOCK_SYMBOL);
        mapPortfolios.put(4, PORTFOLIOS_POSITION_TYPE);

        mapHistory.put(0, HISTORY_DATE);
        mapHistory.put(1, HISTORY_OPEN);
        mapHistory.put(2, HISTORY_HIGH);
        mapHistory.put(3, HISTORY_LOW);
        mapHistory.put(4, HISTORY_CLOSE);
        mapHistory.put(5, HISTORY_VOLUME);
        mapHistory.put(6, HISTORY_ADJ_CLOSE);

        mapCompany.put(0, COMPANY_STOCK_SYMBOL);
        mapCompany.put(1, COMPANY_NAME);
        mapCompany.put(2, COMPANY_SECTOR);
        mapCompany.put(3, COMPANY_INDUSTRY);

        mapIndices.put(0, INDICES_INDEX_NAME);
        mapIndices.put(1, INDICES_NAME_COMPANY);
        mapIndices.put(2, INDICES_CLOSE_VALUE);
        mapIndices.put(3, INDICES_STOCK_SYMBOL);
        mapIndices.put(4, INDICES_IDENTIFIER);
        mapIndices.put(6, INDICES_DATE);

        mapForex.put(0, FOREX_INITIAL_CURRENCY);
        mapForex.put(1, FOREX_TARGET_CURRENCY);
        mapForex.put(2, FOREX_RATE);

        // ////////////////////////////////////////////////
        // Add topics
        DirectoryCSVTopic portfolios = createDirectoryTopic(PORTFOLIOS_TOPIC, env.getProperty("dir.portfolios"), 6, "**.csv", false, mapPortfolios);
        portfolios.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(portfolios);

        DirectoryCSVTopic history = createDirectoryTopic(STOCK_PRICE_HISTORY_TOPIC, env.getProperty("dir.history"), 7, "**PriceHistory_*.csv", true, mapHistory);
        history.getParserConfiguration().setSeparator(COMMA_SEPARATOR);
        csvSource.addTopic(history);

        DirectoryCSVTopic company = createDirectoryTopic(COMPAGNY_INFORMATIONS_TOPIC, env.getProperty("dir.company"), 4, "**.csv", true, mapCompany);
        company.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(company);

        DirectoryCSVTopic gdaxi = createDirectoryTopic(INDICES_TOPIC, env.getProperty("dir.index"), 8, "**.csv", true, mapIndices);
        gdaxi.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(gdaxi);

        DirectoryCSVTopic forex = createDirectoryTopic(FOREX_TOPIC,"", 3, "**forex.csv", false, mapForex);
        forex.getParserConfiguration().setSeparator(BAR_SEPARATOR);
        csvSource.addTopic(forex);

        Properties sourceProps = new Properties();
        sourceProps.put(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, Integer.toString(Runtime.getRuntime().availableProcessors()) );
        csvSource.configure(sourceProps);

        return csvSource;
    }

    @Bean
    @DependsOn(value = "csvSource")
    public CSVMessageChannelFactory csvChannelFactory() {

        // We retrieve the stock symbol of the file being parsing (concerns the Stock Price History)
        List<IColumnCalculator<ILineReader>> csvCalculatedColumnsStockS = new ArrayList<IColumnCalculator<ILineReader>>();
        csvCalculatedColumnsStockS.add(new AColumnCalculator<ILineReader>(HISTORY_STOCK_SYMBOL) {
                                     @Override
                                     public Object compute(IColumnCalculationContext<ILineReader> iColumnCalculationContext) {
                                         String symbol = iColumnCalculationContext.getContext().getCurrentFile().getName();
                                         symbol = symbol.replace("PriceHistory_","");
                                         symbol = symbol.replace(".csv","");
                                         symbol = symbol.replace("-", ".");
                                         return symbol;
                                     }
                                 });

        // We retrieve the fields of the Indices files which will be included in the Portfolio store, that means which
        // will not be contains in the custom Indices stores.
        List<IColumnCalculator<ILineReader>> csvCalculatedColumnsIndices = new ArrayList<IColumnCalculator<ILineReader>>();
        csvCalculatedColumnsIndices.add( new CSVColumnParser(PORTFOLIOS_POSITION_TYPE, "String", 5) );
        csvCalculatedColumnsIndices.add( new CSVColumnParser(PORTFOLIOS_NUMBER_STOCKS, "Integer", 7) );

        CSVMessageChannelFactory channelFactory = new CSVMessageChannelFactory(csvSource(), datastore);
        channelFactory.setCalculatedColumns(STOCK_PRICE_HISTORY_TOPIC, STOCK_PRICE_HISTORY_STORE_NAME, csvCalculatedColumnsStockS);
        channelFactory.setCalculatedColumns(INDICES_TOPIC, INDICES_STORE_NAME, csvCalculatedColumnsIndices);

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

        Collection<IMessageChannel<IFileInfo, ILineReader>> csvChannels = new ArrayList<>();
        csvChannels.add(csvChannelFactory().createChannel(PORTFOLIOS_TOPIC));
        csvChannels.add(csvChannelFactory().createChannel(COMPAGNY_INFORMATIONS_TOPIC));

        long before = System.nanoTime();
        if (!Boolean.parseBoolean(env.getProperty("training.replay"))) {
            ITransactionManager transactionManager = datastore.getTransactionManager();
            transactionManager.startTransaction();
            //fetch the sources and perform a bulk transaction
            csvSource().fetch(csvChannels);
            transactionManager.commitTransaction();
        } else {
            // read data files without sending anything to the datastore (that data is already loaded by the log replayer): those files won't then be considered as new files when enabling real time
        }
        long elapsed = System.nanoTime() - before; // log that somewhere
        return null;
    }

    @Bean
    public IMessageChannel<String, Object> historyChannel() {
        final ITuplePublisher<String> publisher = new AutoCommitTuplePublisher(new TuplePublisher<String>(
                datastore,
                STOCK_PRICE_HISTORY_STORE_NAME));

        return csvChannelFactory().createChannel(STOCK_PRICE_HISTORY_TOPIC, STOCK_PRICE_HISTORY_STORE_NAME, publisher);
    }

    @Bean
    public IMessageChannel<String, Object> indicesChannel() {
        Collection<String> topics = new ArrayList<>();
        topics.add(INDICES_TOPIC);
        topics.add(INDICES_HISTORY_TOPIC);
        Collection<String> stores = new ArrayList();
        stores.add(PORTFOLIOS_STORE_NAME);
        stores.add(INDICES_STORE_NAME);
        Map<String, Integer> nameToIndex = new HashMap<String, Integer>();
        nameToIndex = csvChannelFactory().getTranslator(INDICES_TOPIC, INDICES_STORE_NAME).getColumnIndexes();
        final ITuplePublisher<String> publisher = new AutoCommitTuplePublisher<>(new IndicesTuplePublisher(datastore, stores, nameToIndex));
        return csvChannelFactory().createChannel(INDICES_TOPIC, INDICES_STORE_NAME, publisher);
    }

    @Bean IMessageChannel<String, Object> forexChannel() {
        final ITuplePublisher<String> publisher = new AutoCommitTuplePublisher<>(new ForexTuplePublisher(datastore, FOREX_STORE_NAME));
        return csvChannelFactory().createChannel(FOREX_TOPIC, FOREX_STORE_NAME, publisher);
    }

    /**
     * Real time updates task: once the initial load is done,
     * we start a task to create/update trades
     * @return Void
     * @throws LogWriteException if it fails to listen to csv files.
     */
    @Bean
    @DependsOn(value = { "initialLoad", "indicesChannel", "forexChannel" })
    public Void realTime() {
        // Start real time time update
        csvSource().listen(indicesChannel());
        csvSource().listen(forexChannel());
        csvSource().listen(historyChannel());
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
