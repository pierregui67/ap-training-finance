package com.qfs.training.rivolition.data.index;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Quote {

	private String symbol;
	private String name;
	
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
}
