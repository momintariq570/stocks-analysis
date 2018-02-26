package com.stocks.analysis.data;

public class Trade {
	private String entryDate;
	private String exitDate;
	private double entryPrice;
	private double exitPrice = 0;
	private double maxPrice;
	private boolean isProfitable;
	
	public Trade(String entryDate, double entryPrice, double maxPrice) {
		super();
		this.entryDate = entryDate;
		this.entryPrice = entryPrice;
		this.maxPrice = maxPrice;
	}

	public String getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(String entryDate) {
		this.entryDate = entryDate;
	}

	public String getExitDate() {
		return exitDate;
	}

	public void setExitDate(String exitDate) {
		this.exitDate = exitDate;
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}

	public double getExitPrice() {
		return exitPrice;
	}

	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}

	public double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public boolean isProfitable() {
		return isProfitable;
	}

	public void setProfitable(boolean isProfitable) {
		this.isProfitable = isProfitable;
	}
}
