package com.qfs.training.rivolition.data.sector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class SectorsDownloader {

	public final static String BASE_FOLDER = "src/main/resources/DATA/Secteurs/";
	private final static String FILE_EXTENSION = ".csv";
	
	private final static String BASE_URL = "http://biz.yahoo.com/p/";
	
	private final ArrayList<String> industries;
	
	public static void main(String[] args) {
		try {
			new SectorsDownloader().parseURL(BASE_URL + "s_conameu.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SectorsDownloader() {
		industries = new ArrayList<String>();
	}
	
	private void parseURL(String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(1000000).get();

		//System.out.println(doc.html());
		Elements elements = doc.getElementsByAttribute("nowrap");
		elements = elements.not(":contains(N/A), :contains(lookup)");
		for (Element element : elements) {
			if(element.hasText()) {
				//System.out.println(element.text());
				String link = element.getElementsByAttribute("href").get(0).attr("href");
				parseInterURL(element.text(), BASE_URL + link);
				//System.out.println(link.attr("href"));
			}			
		}
	}
	
	private void parseInterURL(String sector, String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(1000000).get();
		//System.out.println(doc.html());
		Elements elements = doc.getElementsByAttribute("nowrap");
		elements = elements.not(":contains(More Info), :contains(N/A), :contains(lookup)");
		boolean skip = true;
		for (Element element : elements) {
			if(skip) {
				skip = false;
			} else {
				if(element.hasText()) {
					String link = element.child(0).attr("href");
					parseLastURL(sector, element.text(), BASE_URL + link);
				}			
			}
		}
	}
	
	private void parseLastURL(String sector, String industry, String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(1000000).get();
		Pattern pattern = Pattern.compile("(.+)\\(([^)]+)\\)");
		Elements elements = doc.getElementsByAttribute("nowrap");
		elements = elements.not(":contains(N/A), :contains(lookup)");
		String cleanIndustry = industry.replaceAll("/", "");
		String filePath = BASE_FOLDER + sector + "/" + cleanIndustry + FILE_EXTENSION;
		boolean append;
		File file = new File(filePath);
		System.out.println(Files.createDirectories(file.getParentFile().toPath()).toString());
		if(industries.contains(industry)) {
			append = Files.exists(file.toPath());
		} else {
			industries.add(industry);
			append = false;
		}
		System.out.println(file.getCanonicalPath());
		FileWriter writer = new FileWriter(filePath, append);
		for (Element element : elements) {
			if(element.hasText()) {
				Matcher matcher = pattern.matcher(element.text());
				if (matcher.find()) {
					String out = matcher.group(2) + "|" + matcher.group(1) + "|" + sector + "|"+ industry +"\n";
					writer.append(out);
				}
			}			
		}
		writer.flush();
		writer.close();
	}
}
