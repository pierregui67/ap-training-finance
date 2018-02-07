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
import com.qfs.msg.csv.filesystem.impl.SingleFileCSVTopic;
import com.qfs.msg.csv.impl.CSVColumnParser;
import com.qfs.msg.csv.impl.CSVParserConfiguration;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.csv.translator.impl.AColumnCalculator;
import com.qfs.msg.impl.WatcherService;
import com.qfs.sandbox.publisher.impl.IndexTuplePublisher;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.SchemaPrinter;
import com.qfs.store.transaction.ITransactionManager;
import com.qfs.util.timing.impl.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.STRING;
import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;

/**
 * Spring configuration of the Sandbox ActivePivot server.<br>
 * The parameters of the Sandbox ActivePivot server can be quickly changed by modifying the
 * pojo.properties file.
 *
 * @author Quartet FS
 */
@Configuration
public class SourceConfig {

    private static final Logger LOGGER = Logger.getLogger(SourceConfig.class.getSimpleName());

    public static final String PORTFOLIO_TOPIC = "PortfolioTopic";
    public static final String HISTORY_TOPIC = "HistoryTopic";
    public static final String INDEX_TOPIC = "IndexTopic";
    public static final String SECTOR_TOPIC = "SectorTopic";

    @Autowired
    protected Environment env;

    @Autowired
    protected IDatastore datastore;

    // CSV Load
    @Bean
    public IWatcherService watcherService() {
        return new WatcherService();
    }

    @Bean
    public CSVSource csvSource() {
        //define the csv source
        CSVSource csvSource = new CSVSource();

        // define and ad topics to the source
        DirectoryCSVTopic  history = createDirectoryTopic(HISTORY_TOPIC, env.getProperty("dir.history"), 8, "**PriceHistory_*.csv", true);
        history.getParserConfiguration().setSeparator(',');
        csvSource.addTopic(history);

        DirectoryCSVTopic portfolio = createDirectoryTopic(PORTFOLIO_TOPIC, env.getProperty("dir.portfolio"), 6, "**.csv", false);
        portfolio.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(portfolio);

        DirectoryCSVTopic sector = createDirectoryTopic(SECTOR_TOPIC, env.getProperty("dir.sector"), 4, "**.csv", true);
        sector.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(sector);

        SingleFileCSVTopic indexSingle = creatEeSingleFileIndexTopic(INDEX_TOPIC, env.getProperty("file.index"), true);
        indexSingle.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(indexSingle);

        //dfine csv source properties
        Properties sourceProps = new Properties();
        sourceProps.put(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, "4");
        csvSource.configure(sourceProps);

        return csvSource;
    }

    @Bean
    @DependsOn(value = "csvSource")
    public CSVMessageChannelFactory csvChannelFactory() {
        //create channel factory
        CSVMessageChannelFactory channelFactory = new CSVMessageChannelFactory(csvSource(), datastore);

        // create and CSV calculated columns
        List<IColumnCalculator<ILineReader>> csvCalculatedColumns = new ArrayList<IColumnCalculator<ILineReader>>();
        csvCalculatedColumns.add(new AColumnCalculator<ILineReader>(STOCK_SYMBOL) {
            @Override
            public Object compute(IColumnCalculationContext<ILineReader> iColumnCalculationContext) {
                //get stock symbol from file name in ColumnCalculationContext
                String filename = iColumnCalculationContext.getContext().getCurrentFile().getName();
                String stockSymbol = filename
                                        .replace("PriceHistory_","")
                                        .replace(".csv","")
                                        .replace("-",".");
                return stockSymbol;
            }
        });
        channelFactory.setCalculatedColumns(HISTORY_TOPIC, HISTORY_STORE, csvCalculatedColumns);
        return channelFactory;
    }

    @Bean
    @DependsOn(value = "startManager")
    public Void initialLoad() throws Exception {
        //create csv channel
        Collection<IMessageChannel<IFileInfo, ILineReader>> csvChannels = new ArrayList<>();
        csvChannels.add(csvChannelFactory().createChannel(HISTORY_TOPIC, HISTORY_STORE));
        csvChannels.add(csvChannelFactory().createChannel(PORTFOLIO_TOPIC, PORTFOLIOS_STORE));
        csvChannels.add(csvChannelFactory().createChannel(SECTOR_TOPIC, SECTORS_STORE));

        List<IColumnCalculator> indexColumnCalulators = Arrays.asList(
                new CSVColumnParser(COMPANY, STRING,1),
                new CSVColumnParser(CLOSE_PRICE, DOUBLE,2),
                new CSVColumnParser(IDENTIFIER, STRING,4)
        );


        csvChannels.add(csvChannelFactory().createChannel(INDEX_TOPIC, PORTFOLIOS_STORE,
                            new IndexTuplePublisher(datastore, Arrays.asList(PORTFOLIOS_STORE, CUSTOM_INDEX_DATA_STORE)), indexColumnCalulators));

        long before = System.nanoTime();
        if (!Boolean.parseBoolean(env.getProperty("training.replay"))) {
            ITransactionManager transactionManager = datastore.getTransactionManager();
            transactionManager.startTransaction();
            //fetch tHE sources and perform a bulk transaction
            csvSource().fetch(csvChannels);
            transactionManager.commitTransaction();
        } else {
            // read data files without sending anything to the datastore (that data is already loaded by the log replayer): those files won't then be considered as new files when enabling real time
        }

        long elapsed = System.nanoTime() - before;
        LOGGER.info("All sources fetched in " + elapsed + " ms");

        printStoreSizes();

        return null;
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

    private SingleFileCSVTopic creatEeSingleFileIndexTopic(String topic, String file, boolean skipFirstLine) {
  /*      CSVParserConfiguration cfg = new CSVParserConfiguration(
//                Arrays.asList("INDEX","COMPANY","PRICE","SYMBOL","IDENTIFIER","TYPE","DATE","VOLUME")
                Arrays.asList(PORTFOLIO_TYPE, COMPANY, CLOSE_PRICE, STOCK_SYMBOL, IDENTIFIER, STOCK_TYPE, DATE, QUANTITY)
        );*/
        Map<Integer,String> indexName = new HashMap<>();
        indexName.put(0,PORTFOLIO_TYPE);
        indexName.put(1,COMPANY);
        indexName.put(2,CLOSE_PRICE);
        indexName.put(3,STOCK_SYMBOL);
        indexName.put(4,IDENTIFIER);
        indexName.put(5,POSITION_TYPE);
        indexName.put(6,DATE);
        indexName.put(7,QUANTITY);

        CSVParserConfiguration cfg = new CSVParserConfiguration(8);
        cfg.setColumns(indexName);

        if (skipFirstLine) {
            cfg.setNumberSkippedLines(1);//skip the first line
        }
        String baseDir = env.getProperty("dir.base");
        return new SingleFileCSVTopic(topic, cfg, Paths.get(baseDir,file),watcherService());
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
