package com.qfs.training.rivolition.data.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.qfs.training.rivolition.data.history.HistoryDownloader;
import com.qfs.training.rivolition.data.index.JsonIndexDownloader;
import com.qfs.training.rivolition.data.sector.SectorsDownloader;

/**
 * Class downloading all the data needed
 * @author Perseverance
 *
 */
public class DataDownlader {
	
	public static final String HISTORY_ARG = "history";
	public static final String JSON_INDEX_ARG = "index";
	public static final String SECTOR_ARG = "sector";

	/**
	 * Download data
	 * @param args data to be downloaded. If null, everything is downloaded
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{SECTOR_ARG, JSON_INDEX_ARG, HISTORY_ARG};
		}
		
		List<String> argsList = Arrays.asList(args);
		if (argsList.contains(SECTOR_ARG)) {
			SectorsDownloader.main(null);
		}
		if (argsList.contains(JSON_INDEX_ARG)) {
			JsonIndexDownloader.main(null);
		}
		if (argsList.contains(HISTORY_ARG)) {
			HistoryDownloader.main(null);
		}
	}

}
