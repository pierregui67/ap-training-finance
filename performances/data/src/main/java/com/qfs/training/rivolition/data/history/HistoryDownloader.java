package com.qfs.training.rivolition.data.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status.Family;

import com.qfs.training.rivolition.data.index.JsonIndexDownloader;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class downloading historical data CSV of each portfolios symbol
 * @author Perseverance
 *
 */
public class HistoryDownloader {

	/** Part of the History CSV download URL before the symbol name */
	final String prefixURL = "http://ichart.finance.yahoo.com/table.csv?s=";
	/** Part of the History CSV download URL after the symbol name */
	final String suffixURL = "&c=2014";
	/** History folder path*/ 
	public static String HISTORY_FOLDER = "src/main/resources/DATA/History/";
	Client client;

	/**
	 * Constructor
	 */
	public HistoryDownloader() {
		client = Client.create();
	}

	/**
	 * Download the History CSV file of a symbol list
	 * @param symbols
	 */
	public void downloadHistorybySymbol(List<String> symbols) {
		for(String symbol : symbols){
			downloadSymbolCSV(symbol);
		}		
	}

	/**
	 * Download the History CSV file from a symbol
	 * @param symbol
	 */
	private void downloadSymbolCSV(String symbol) {
		// Reaching the URL
		WebResource resource = client.resource(prefixURL + symbol + suffixURL);
		ClientResponse response = resource.accept("application/text").get(ClientResponse.class);

		// Check the URL response
		if (response.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
			InputStream is = response.getEntityInputStream();
			System.out.print("Writing CSV of symbol " + symbol + "...");
			writeFromStream(is, symbol);
			System.out.println(" Done");
		} else {
			System.out.println("Failed downloading " + symbol + " CSV : error "+ response.getStatus());
		}
	}
	
	/**
	 * Write the CSV from an InputStream
	 * @param is
	 * @param symbol
	 */
	private void writeFromStream(InputStream is, String symbol) {
		try {
			// Creating the directory
			File directory = new File(HISTORY_FOLDER);
			directory.mkdirs();
			
			// Writing the file
			FileWriter writer = new FileWriter(directory.getPath() + File.separator + "PriceHistory_" + symbol.replace('.', '-') + ".csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				writer.write(line + "\n");
			}
			br.close();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Getting the History CSVs
	 * @param args
	 */
	public static void main(String[] args) {
		HistoryDownloader client = new HistoryDownloader();
		JsonIndexDownloader jsonClient = new JsonIndexDownloader();
		client.downloadHistorybySymbol(jsonClient.getSymbols());
	}
}
