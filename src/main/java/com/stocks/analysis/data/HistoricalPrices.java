package com.stocks.analysis.data;

/**
 * POJO which contains OHLC prices
 * and volume for a particular date
 * @author momintariq
 *
 */
public class HistoricalPrices {

	private String date;
	private double open;
	private double high;
	private double low;
	private double close;
	private int volume;
	
	public HistoricalPrices(String date, double open, double high, double low, double close, int volume) {
		super();
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

	/**
	 * Getter for the date
	 * @return date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Setter for the date
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Getter for the open price
	 * @return open price
	 */
	public double getOpen() {
		return open;
	}

	/**
	 * Setter for the open price
	 * @param open
	 */
	public void setOpen(double open) {
		this.open = open;
	}

	/**
	 * Getter for the high price
	 * @return high price
	 */
	public double getHigh() {
		return high;
	}

	/**
	 * Setter for the high price
	 * @param high
	 */
	public void setHigh(double high) {
		this.high = high;
	}

	/**
	 * Getter for the low price
	 * @return low price
	 */
	public double getLow() {
		return low;
	}

	/**
	 * Setter for the low price
	 * @param low
	 */
	public void setLow(double low) {
		this.low = low;
	}

	/**
	 * Getter for the close price
	 * @return close price
	 */
	public double getClose() {
		return close;
	}

	/**
	 * Setter for the close price
	 * @param close
	 */
	public void setClose(double close) {
		this.close = close;
	}

	/**
	 * Getter for the volume
	 * @return volume
	 */
	public int getVolume() {
		return volume;
	}

	/**
	 * Setter for the volume
	 * @param volume
	 */
	public void setVolume(int volume) {
		this.volume = volume;
	}
}
