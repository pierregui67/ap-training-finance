package com.qfs.training.rivolition.data.index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.qfs.training.rivolition.data.index.Position;

public class PositionExpander {	

	private final static String CSV_SEPARATOR = "|";
	private final static String FILE_NAME_SUFFIX = ".csv";
	private final static String PORTFOLIO_FOLDER = "src/main/resources/DATA/Portfolios/";
	
	private ArrayList<Position> positions;

	public PositionExpander() {
		positions = new ArrayList<Position>();
	}
	
	public void addPosition(Position position) {
		positions.add(position);
	}
	
	public void expand() {
		// create the directories
		File initial = new File(PORTFOLIO_FOLDER + "/Initial/");
		initial.mkdirs();
		File realTime = new File(PORTFOLIO_FOLDER + "/RT/");
		realTime.mkdirs();
		
		ArrayList<Position> positionsToAdd = new ArrayList<Position>();
		for (Position position : positions) {
			Date date = position.getDate();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			Date nextDate = getNextDate(position);
			if(nextDate !=null) {
				while (c.getTime().before(nextDate)){
					c.add(Calendar.DATE, 1);
					Position positionClone = new Position();
					positionClone.setName(position.getName());
					positionClone.setDate(c.getTime());
					positionClone.setFund(position.getFund());
					positionClone.setQuantity(position.getQuantity());
					positionClone.setSymbol(position.getSymbol());
					positionsToAdd.add(positionClone);
				}
			}
		}
		for (Position position : positionsToAdd) {
			positions.add(position);
		}
		writeToCSV();
	}
	
	private void writeToCSV() {
		ArrayList<String> funds = new ArrayList<String>();
		for (Position position : positions) {
			File file = new File(PORTFOLIO_FOLDER + "/Initial/" + position.getFund()+FILE_NAME_SUFFIX);
			boolean append;
			if(funds.contains(position.getFund())) {
				append = file.exists();
			} else {
				funds.add(position.getFund());
				append = false;
			}
			try {
				FileWriter writer = new FileWriter(file, append);
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				writer.write(dateFormat.format(position.getDate())
						+CSV_SEPARATOR+position.getFund()
						+CSV_SEPARATOR+position.getQuantity()
						+CSV_SEPARATOR+position.getSymbol()
						+CSV_SEPARATOR+"Regular"
						+CSV_SEPARATOR+"\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Date getNextDate(Position position) {
		Date minDate = position.getDate();
		
		Date nextDate = null;
		
		for(int i=0; i<positions.size(); i++) {
			Position p = positions.get(i);
			Date date = p.getDate();
			if(!p.getFund().equals(position.getFund())) {
				continue;
			}
			if(!p.getSymbol().equals(position.getSymbol())) {
				continue;
			}
			if(date.after(minDate)) {
				if((nextDate==null)||(date.before(nextDate))) {
					nextDate = date;
				}
			}
		}
		
		return nextDate;
	}
}
