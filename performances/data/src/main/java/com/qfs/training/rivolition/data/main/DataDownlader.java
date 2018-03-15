package com.qfs.training.rivolition.data.main;

import com.qfs.training.rivolition.data.main.download.Downloader;
import com.qfs.training.rivolition.data.main.download.IndexDownloader;
import com.qfs.training.rivolition.data.main.download.IndicesHistoryDownloader;
import com.qfs.training.rivolition.data.main.download.StockPriceHistoryDownloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class downloading all the data needed
 * @author Perseverance
 *
 */
public class DataDownlader {
	
	public static final String HISTORY_ARG = "history";
	public static final String JSON_INDEX_ARG = "index";
	public static final String SECTOR_ARG = "sector";

	public static final String BASE_FOLDER = "activepivot-server/src/main/resources/data/";

	/**
	 * Download data
	 * @param args data to be downloaded. If null, everything is downloaded
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{SECTOR_ARG, JSON_INDEX_ARG, HISTORY_ARG};
		}

        Path currentRelativePath = Paths.get("");
        String s = Paths.get("").toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
		
		List<String> argsList = Arrays.asList(args);
		if (argsList.contains(SECTOR_ARG)) {
			//SectorsDownloader.main(BASE_FOLDER);
		}
		if (argsList.contains(JSON_INDEX_ARG)) {
			//JsonIndexDownloader.main(BASE_FOLDER);
		}
		if (argsList.contains(HISTORY_ARG)) {
			//HistoryDownloader.main(BASE_FOLDER);
		}
		Downloader download;
		/*download = new IndexDownloader(BASE_FOLDER);
		download.main();*/
		/*download = new StockPriceHistoryDownloader(BASE_FOLDER);
		download.main();*/
        download = new IndicesHistoryDownloader(BASE_FOLDER);
		download.main();
	}

}
