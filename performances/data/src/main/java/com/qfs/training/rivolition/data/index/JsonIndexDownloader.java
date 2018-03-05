package com.qfs.training.rivolition.data.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qfs.training.rivolition.data.sector.SectorsDownloader;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class retrieving data from indices defined in the data.properties file
 * @author Perseverance
 *
 */
public class JsonIndexDownloader {
	
	private static final String PREFIX_URL = "http://finance.yahoo.com/webservice/v1/symbols/@%5E";
	private static final String SUFFIX_URL = "/quote?format=json";
	private static final String PROPERTIES_FILE = "src/main/resources/data.properties";
	private static final String INDEX_FOLDER = "src/main/resources/DATA/Index";
	private static final String FILE_NAME_SUFFIX = ".csv";
	private static final String CSV_SEPARATOR = "|";
	private static String[] INDICES;
	private final Client client;
	private WebResource webResource;
	private JsonArray quoteArray;
	private ArrayList<String> authorizedSymbols;
	
	public static void main(String[] args) {
		JsonIndexDownloader client = new JsonIndexDownloader();
		
		// create the different index csv files
		client.writeToCSV();
		
		// create the basic portfolios
		client.fillQueue();	
	}
	
	public JsonIndexDownloader() {
		initProperties();
		authorizedSymbols = listAuthorizedSymbols();
		client = Client.create();
	}
	
	/**
	 * Get properties from the data.properties file
	 */
	public void initProperties() {
	 
		try {
			
			Properties prop = new Properties();
			InputStream input = null;
			input = new FileInputStream(PROPERTIES_FILE);
	 
			// load the properties file
			prop.load(input);
	 
			// get the indices property values and store it in INDICES array
			String indicesString = prop.getProperty("indices").replaceAll("\\s+", "");
			INDICES = indicesString.split(",");
			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getResponseText(String url) throws RuntimeException {
		webResource=client.resource(url);
		ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
		if (response.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ response.getStatus());
		}	 
		String output = response.getEntity(String.class);
		return output;
	}
		
	public JsonArray getQuoteArray(String fundName) {
		String jsonString = getResponseText(PREFIX_URL + fundName + SUFFIX_URL);
		
		//Adding the fund name to the Json String
		jsonString = jsonString.replaceAll("\"name\" : ", "\"fund\" : \"" + fundName + "\",\n\"name\" : ");
		
		JsonParser parser = new JsonParser();
		JsonObject json= parser.parse(jsonString).getAsJsonObject();
		JsonArray array = json.getAsJsonObject("list").getAsJsonArray("resources");
		
		return array;
	}
	
	public JsonArray getQuoteArray() {
		if(quoteArray==null) {
			quoteArray = new JsonArray();
			for (String index : INDICES) {
				quoteArray.addAll(getQuoteArray(index));
			}
		}
		return quoteArray;
	}

	public List<String> getSymbols() {
		List<String> symbols = new ArrayList<String>();
		JsonArray array = getQuoteArray();
		for(int i=0; i<array.size(); i++) {
			String symbol = array.get(i).getAsJsonObject().getAsJsonObject("resource").getAsJsonObject("fields").get("symbol").getAsString();
			if(authorizedSymbols.contains(symbol)) {
				symbols.add(symbol);
			}
		}
		return symbols;
	}
	
	public void fillQueue() {
		JsonArray quoteJsonArray = getQuoteArray();
		PositionExpander positionExpander = new PositionExpander();
		if(quoteJsonArray==null) {
			System.out.println("NULL");
		}
		int moneyPerSymbol = 10000;
		int moneyAdjustment = 1000;
		for(int i=0; i<quoteJsonArray.size(); i++) {
			JsonObject quoteJsonObject = quoteJsonArray.get(i).getAsJsonObject().getAsJsonObject("resource").getAsJsonObject("fields");
			
			int curMoney = moneyPerSymbol;
			
			if(i<(quoteJsonArray.size()-1))  {
				curMoney = ((i%2)==0)?(curMoney-moneyAdjustment):(curMoney+moneyAdjustment);
			}
			
			String symbol = quoteJsonObject.get("symbol").getAsString();
			if(!authorizedSymbols.contains(symbol)) {
				continue;
			}
			float price = quoteJsonObject.get("price").getAsFloat();
			int priceInt = (int) price;
			
			Quote quote = new Quote();
			quote.setName(quoteJsonObject.get("name").getAsString());
			quote.setSymbol(symbol);
						
			Position position = new Position();
			position.setName(quote.getName());
			position.setDate("2015-04-27");
			position.setFund("exemple");
			position.setBenchmark("benchmark");
			position.setQuantity(curMoney/priceInt);
			position.setSymbol(symbol);
			
			Position position2 = new Position();
			position2.setName(quote.getName());
			position2.setDate("2015-04-22");
			position2.setFund("exemple");
			position2.setBenchmark("benchmark");
			position2.setQuantity(curMoney/priceInt);
			position2.setSymbol(symbol);	
			
			Position position3 = new Position();
			position3.setName(quote.getName());
			position3.setDate("2015-03-22");
			position3.setFund("exemple");
			position3.setBenchmark("benchmark");
			position3.setQuantity(curMoney/priceInt);
			position3.setSymbol(symbol);
			
			positionExpander.addPosition(position);
			positionExpander.addPosition(position2);
			positionExpander.addPosition(position3);
			
			Position benchmark = new Position();
			benchmark.setName(quote.getName());
			benchmark.setDate("2015-04-27");
			benchmark.setFund("benchmark");
			benchmark.setBenchmark("benchmark");
			benchmark.setQuantity(moneyPerSymbol/priceInt);
			benchmark.setSymbol(symbol);
			
			Position benchmark2 = new Position();
			benchmark2.setName(quote.getName());
			benchmark2.setDate("2015-04-22");
			benchmark2.setFund("benchmark");
			benchmark2.setBenchmark("benchmark");
			benchmark2.setQuantity(moneyPerSymbol/priceInt);
			benchmark2.setSymbol(symbol);	
			
			Position benchmark3 = new Position();
			benchmark3.setName(quote.getName());
			benchmark3.setDate("2015-03-22");
			benchmark3.setFund("benchmark");
			benchmark3.setBenchmark("benchmark");
			benchmark3.setQuantity(moneyPerSymbol/priceInt);
			benchmark3.setSymbol(symbol);
			
			positionExpander.addPosition(benchmark);
			positionExpander.addPosition(benchmark2);
			positionExpander.addPosition(benchmark3);
		}
		positionExpander.expand();
	}
	
	private ArrayList<String> listAuthorizedSymbols() {
		ArrayList<String> authorizedSymbols = new ArrayList<String>();
		File file = new File(SectorsDownloader.BASE_FOLDER);
		// XXX downloading the base folder in case of it doesn't exist
		if (!file .exists()) {
			String[] args = null;
			try {
				SectorsDownloader.main(args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		listAuthorizedSymbols(file, authorizedSymbols);
		System.out.println("Done");
		return authorizedSymbols;
	}
	
	private void listAuthorizedSymbols(File file, ArrayList<String> authorizedSymbols) {
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\\|";
	 
		if(!file.isDirectory()) {
			try {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					
					String[] lineFrag = line.split(cvsSplitBy);
					authorizedSymbols.add(lineFrag[0]);
		 
				}
		 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			File[] files = file.listFiles();
			for (File sector : files) {
				listAuthorizedSymbols(sector, authorizedSymbols);
			}
		}
	}
	
	
	/**
	 *  Writes all the different indices to individual .csv files
	 */
	private void writeToCSV() {		
		List<String> funds = new ArrayList<String>();
		JsonArray array = getQuoteArray();
		for(int i=0; i<array.size(); i++) {
			boolean append = true;
			JsonObject fields = array.get(i).getAsJsonObject().getAsJsonObject("resource").getAsJsonObject("fields");
			String fund = fields.get("fund").getAsString();
			if(!funds.contains(fund)) {
				funds.add(fund);
				append = false;
			}
			File file = new File(INDEX_FOLDER + "/" + fund + FILE_NAME_SUFFIX);
			file.getParentFile().mkdirs();
			try {
				FileWriter writer = new FileWriter(file, append);
				StringBuilder builder = new StringBuilder(); 
				Set<Entry<String, JsonElement>> set = fields.entrySet();
				for(Entry<String, JsonElement> entry : set) {
					builder.append(entry.getValue().toString().replaceAll("\"", "") + CSV_SEPARATOR);
				}
				builder.replace(builder.length()-1, builder.length(), "\n");
				writer.write(builder.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

}
