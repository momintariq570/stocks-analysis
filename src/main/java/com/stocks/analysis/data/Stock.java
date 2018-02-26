package com.stocks.analysis.data;

import java.util.List;

/**
 * Pojo containing the ticker
 * and historical prices
 * @author momintariq
 *
 */
public class Stock {

	private String ticker;
	private List<HistoricalPrices> historicalPrices;
	
	/**
	 * Constructor
	 * @param ticker
	 * @param historicalPrices
	 */
	public Stock(String ticker, List<HistoricalPrices> historicalPrices) {
		super();
		this.ticker = ticker;
		this.historicalPrices = historicalPrices;
	}

	/**
	 * Getter for ticker
	 * @return ticker
	 */
	public String getTicker() {
		return ticker;
	}

	/**
	 * Setter for ticker
	 * @param ticker
	 */
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	/**
	 * Getter for historical prices
	 * @return historical prices
	 */
	public List<HistoricalPrices> getHistoricalPrices() {
		return historicalPrices;
	}

	/**
	 * Setter for historical prices
	 * @param historicalPrices
	 */
	public void setHistoricalPrices(List<HistoricalPrices> historicalPrices) {
		this.historicalPrices = historicalPrices;
	}
}
