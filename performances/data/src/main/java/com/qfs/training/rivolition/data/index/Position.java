package com.qfs.training.rivolition.data.index;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Position {
	private String symbol;
	private String name;
	private String Fund;
	private String Benchmark;
	private Date date;
	private int quantity;
	
	public Position() {
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getFund() {
		return Fund;
	}
	public void setFund(String fund) {
		Fund = fund;
	}
	public String getBenchmark() {
		return Benchmark;
	}
	public void setBenchmark(String benchmark) {
		Benchmark = benchmark;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(String date) {
//	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    try {
			this.date = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void setDate(Date date) {
			this.date = date;
	}	
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
