package com.stocks.analysis.data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jayway.jsonpath.internal.function.numeric.Average;

/**
 * Calculates various technical
 * indicators for a given stock
 * @author momintariq
 *
 */
@Component
public class Technicals {
	
	public enum TechnicalType {
		SMA, EMA, RSI, AROON, CCI
	}
	
	/**
	 * Calculates simple moving average
	 * @param stock company's stock
	 * @param period average period
	 * @param offset starting point
	 * @return simple moving average
	 */
	public double sma(final Stock stock, final int period, final int offset) {
		double sum = 0;
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		int startingPoint = offset;
		int endingPoint = startingPoint - period + 1;
		try {
			for(int i = startingPoint; i >= endingPoint; i--) {
				sum += historicalPrices.get(i).getClose();
			}	
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
		return sum / period;
	}
	
	/**
	 * Calculates exponential moving average
	 * @param stock Stock object containing historical prices
	 * @param period average period
	 * @param offset ending point
	 * @return exponential moving average
	 */
	public double ema(final Stock stock, final int period, final int offset) {
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		int startingPoint = period;
		double previousEma = sma(stock, period, startingPoint - 1);
		if(previousEma == 0) {
			return 0;
		}
		double currentEma = previousEma;
		double multiplier = 2 / ((double)period + 1);
		for(int i = startingPoint; i <= offset; i++) {
			double price = historicalPrices.get(i).getClose();
			currentEma = (price * multiplier) + (previousEma * (1 - multiplier));
			previousEma = currentEma;
		}
		return currentEma;
	}
	
	/**
	 * Calculates relative strength index
	 * @param stock Stock object containing historical prices
	 * @param period average period
	 * @param offset ending point
	 * @return
	 */
	public double rsi(final Stock stock, final int period, final int offset) {
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		double rsi = 0;
		int startingPoint = period + 1;
		int endingPoint = offset;
		double change;
		double currGain;
		double currLoss;
		double totalGain = 0;
		double totalLoss = 0;
		double prevAvgGain = 0;
		double currAvgGain = 0;
		double prevAvgLoss = 0;
		double currAvgLoss = 0;
		double rs;
		
		if(offset < period) {
			return -1;
		}
		
		for(int i = 1; i <= period; i++) {
			change = historicalPrices.get(i).getClose() - historicalPrices.get(i - 1).getClose();
			if(change > 0) {
				totalGain += change;
			} else {
				totalLoss += -1 * change;
			}
		}
		
		currAvgGain = totalGain / period;
		currAvgLoss = totalLoss / period;
		prevAvgGain = currAvgGain;
		prevAvgLoss = currAvgLoss;
		rs = currAvgGain / currAvgLoss;
		
		if(currAvgLoss == 0) {
			rsi = 0;
		} else {
			rsi = 100 - (100 / (1 + rs));
		}
		
		for(int i = startingPoint; i <= endingPoint; i++) {
			change = historicalPrices.get(i).getClose() - historicalPrices.get(i - 1).getClose();
			if(change < 0) {
				currLoss = -1 * change;
				currGain = 0;
			} else {
				currGain = change;
				currLoss = 0;
			}
			currAvgGain = ((prevAvgGain * (period - 1)) + currGain) / period;
			currAvgLoss = ((prevAvgLoss * (period - 1)) + currLoss) / period;
			prevAvgGain = currAvgGain;
			prevAvgLoss = currAvgLoss;
			rs = currAvgGain / currAvgLoss;
			if(currAvgLoss == 0) {
				rsi = 0;
			} else {
				rsi = 100 - (100 / (1 + rs));
			}
		}
		
		return rsi;
	}
	
	public double macd(final Stock stock, final int offset) {
		return ema(stock, 12, offset) - ema(stock, 26, offset); 
	}
	
	/**
	 * Calculates commodity channel index
	 * @param stock Stock object containing historical prices
	 * @param period average period
	 * @param offset ending point
	 * @return
	 */
	public double cci(final Stock stock, final int period, final int offset) {
		List<Double> typicalPrices = new ArrayList<>();
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		int startingPoint = offset - period + 1;
		int endingPoint = offset;
		
		for(int i = startingPoint; i <= endingPoint; i++) {
			double high = historicalPrices.get(i).getHigh();
			double low = historicalPrices.get(i).getLow();
			double close = historicalPrices.get(i).getClose();
			double typicalPrice = (high + low + close) / 3;
			typicalPrices.add(typicalPrice);
		}
		
		double avgTypicalPrice = typicalPrices.stream()
				.mapToDouble(val -> val)
				.average()
				.getAsDouble();
		
		double meanDeviation = typicalPrices.stream()
				.mapToDouble(val -> Math.abs(avgTypicalPrice - val))
				.sum() / period;
		
		double cci = (typicalPrices.get(period - 1) - avgTypicalPrice) / (0.015 * meanDeviation);
		
		return cci;
	}
	
	/**
	 * Calculates standard deviation of closing historical prices
	 * @param stock Stock object containing historical prices
	 * @param period duration
	 * @param offset starting point
	 * @return standard deviation
	 */
	private double stdev(final Stock stock, final int period, final int offset) {
		double mean = sma(stock, period, offset);
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		
		int startingPoint = offset;
		int endingPoint = startingPoint - period + 1;
		double meanDeviationSquaredSum = 0;
		try {
			for(int i = startingPoint; i >= endingPoint; i--) {
				meanDeviationSquaredSum += Math.pow((historicalPrices.get(i).getClose() - mean), 2);
			}	
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
		return Math.sqrt(meanDeviationSquaredSum / period);
	}
	
	/**
	 * Calculates upper part of bollinger bands
	 * @param stock Stock object containing historical prices
	 * @param period duration
	 * @param offset starting point
	 * @return upper part of bollinger bands
	 */
	public double bollingerUp(final Stock stock, final int period, final int offset) {
		double mean = sma(stock, period, offset);
		double stdev = stdev(stock, period, offset);
		return mean + (stdev * 2);
	}
	
	/**
	 * Calculates lower part of bollinger bands
	 * @param stock Stock object containing historical prices
	 * @param period duration
	 * @param offset starting point
	 * @return lower part of bollinger bands
	 */
	public double bollingerDown(final Stock stock, final int period, final int offset) {
		double mean = sma(stock, period, offset);
		double stdev = stdev(stock, period, offset);
		return mean - (stdev * 2);
	}
}
