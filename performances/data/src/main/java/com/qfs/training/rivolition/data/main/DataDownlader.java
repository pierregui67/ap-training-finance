package com.qfs.training.rivolition.data.main;

import com.qfs.training.rivolition.data.main.download.*;

/**
 * Class downloading all the data needed
 * @author Perseverance
 *
 */
public class DataDownlader {

	public static final String BASE_FOLDER = "activepivot-server/src/main/resources/data/";

	/**
	 * Download data
	 * @param args data to be downloaded. If null, everything is downloaded
	 */
	public static void main(String[] args) {

		Downloader download;
		download = new IndexDownloader(BASE_FOLDER);
		download.main();
		download = new StockPriceHistoryDownloader(BASE_FOLDER);
		download.main();
        download = new IndicesHistoryDownloader(BASE_FOLDER);
		download.main();
        download = new SectorDownloader(BASE_FOLDER);
        download.main();
	}

}
