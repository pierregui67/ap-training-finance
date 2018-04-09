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
import java.util.*;

import static com.qfs.sandbox.cfg.impl.DatastoreConfig.*;


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
        // Map index to fields

        // portfolio mapping
        Map <Integer, String> mapPortfolios = new HashMap<Integer, String>();
        mapPortfolios.put(0, PORTFOLIOS__DATE);
        mapPortfolios.put(1, PORTFOLIOS__PORTFOLIO_TYPE);
        mapPortfolios.put(2, PORTFOLIOS__NUMBER_STOCKS);
        mapPortfolios.put(3, PORTFOLIOS__STOCK_SYMBOL);
        mapPortfolios.put(4, PORTFOLIOS__POSITION_TYPE);

        Map <Integer, String> mapStockPriceHistory = new HashMap<Integer, String>();
        mapStockPriceHistory.put(0, STOCK_PRICE_HISTORY__DATE);
        mapStockPriceHistory.put(1, STOCK_PRICE_HISTORY__OPEN);
        mapStockPriceHistory.put(2, STOCK_PRICE_HISTORY__HIGH);
        mapStockPriceHistory.put(3, STOCK_PRICE_HISTORY__LOW);
        mapStockPriceHistory.put(4, STOCK_PRICE_HISTORY__CLOSE);
        mapStockPriceHistory.put(5, STOCK_PRICE_HISTORY__VOLUME);
        mapStockPriceHistory.put(6, STOCK_PRICE_HISTORY__ADJ_CLOSE);

        Map <Integer, String> mapSectorsIndustryCompany = new HashMap<Integer, String>();
        mapSectorsIndustryCompany.put(0, SECTORS_INDUSTRY_COMPANY__STOCK_SYMBOL);
        mapSectorsIndustryCompany.put(1, SECTORS_INDUSTRY_COMPANY__COMPANY_NAME);
        mapSectorsIndustryCompany.put(2, SECTORS_INDUSTRY_COMPANY__SECTOR);
        mapSectorsIndustryCompany.put(3, SECTORS_INDUSTRY_COMPANY__INDUSTRY);


        // /////////////////////////////////////
        // Add topics to your source

		DirectoryCSVTopic history = createDirectoryTopic(STOCK_PRICE_HISTORY_TOPIC, env.getProperty("dir.history"), 7, "**PriceHistory_*.csv", true, mapStockPriceHistory);
		history.getParserConfiguration().setSeparator(',');
		csvSource.addTopic(history);

        DirectoryCSVTopic sectors = createDirectoryTopic(SECTOR_TOPIC, env.getProperty("dir.sectors"), 4, "**.csv", true, mapSectorsIndustryCompany);
        sectors.getParserConfiguration().setSeparator('|');
        csvSource.addTopic(sectors);

        DirectoryCSVTopic portfolios = createDirectoryTopic(PORTFOLIOS_TOPIC, env.getProperty("dir.portfolios"), 6, "**.csv", false, mapPortfolios);
        portfolios.getParserConfiguration().setSeparator('|');
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

        csvCalculatedColumns.add(new AColumnCalculator<ILineReader>(STOCK_PRICE_HISTORY__STOCK_SYMBOL) {
            @Override
            public Object compute(IColumnCalculationContext<ILineReader> iColumnCalculationContext) {
                String symbol = iColumnCalculationContext.getContext().getCurrentFile().getName();
                symbol = symbol.replace("PriceHistory_", "");
                symbol = symbol.replace(".csv", "");
                return symbol;
            }
        }); // look for new FileNameCalculator(),

//        csvCalculatedColumns.add(new FilePathCalculator());

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
        csvChannels.add(csvChannelFactory().createChannel(SECTOR_TOPIC));



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
