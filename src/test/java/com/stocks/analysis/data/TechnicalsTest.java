package com.stocks.analysis.data;

import static org.assertj.core.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TechnicalsTest {
	
	private Technicals technicals;
	private List<HistoricalPrices> historicalPrices;
	
	@Before
	public void setup() {
		technicals = new Technicals();
		historicalPrices = new ArrayList<>();
		historicalPrices.add(new HistoricalPrices("2018-02-12", 9.0, 9.0, 9.0, 9.0, 1));
		historicalPrices.add(new HistoricalPrices("2018-02-13", 23.0, 23.0, 23.0, 23.0, 2));
		historicalPrices.add(new HistoricalPrices("2018-02-14", 46.0, 46.0, 46.0, 46.0, 3));
		historicalPrices.add(new HistoricalPrices("2018-02-15", 35.0, 35.0, 35.0, 35.0, 4));
		historicalPrices.add(new HistoricalPrices("2018-02-16", 14.0, 14.0, 14.0, 14.0, 5));
		historicalPrices.add(new HistoricalPrices("2018-02-17", 29.0, 29.0, 29.0, 29.0, 6));
		historicalPrices.add(new HistoricalPrices("2018-02-18", 33.0, 33.0, 33.0, 33.0, 7));
		historicalPrices.add(new HistoricalPrices("2018-02-19", 55.0, 55.0, 55.0, 55.0, 8));
		historicalPrices.add(new HistoricalPrices("2018-02-20", 73.0, 73.0, 73.0, 73.0, 9));
		historicalPrices.add(new HistoricalPrices("2018-02-15", 11.0, 11.0, 11.0, 11.0, 10));
		historicalPrices.add(new HistoricalPrices("2018-02-16", 28.0, 28.0, 28.0, 28.0, 11));
		historicalPrices.add(new HistoricalPrices("2018-02-17", 41.0, 41.0, 41.0, 41.0, 12));
		historicalPrices.add(new HistoricalPrices("2018-02-18", 79.0, 79.0, 79.0, 79.0, 13));
		historicalPrices.add(new HistoricalPrices("2018-02-19", 52.0, 52.0, 52.0, 52.0, 14));
		historicalPrices.add(new HistoricalPrices("2018-02-20", 21.0, 21.0, 21.0, 21.0, 15));
		historicalPrices.add(new HistoricalPrices("2018-02-21", 24.0, 24.0, 24.0, 24.0, 16));
	}
	
	@Test
	public void testSma() {
		int period;
		int offset;
		double actual;
		double expected;
		Stock stock = new Stock("ABCD", historicalPrices);
		
		period = 1;
		offset = 0;
		actual = technicals.sma(stock, period, offset);
		expected = (9.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 1;
		offset = 8;
		actual = technicals.sma(stock, period, offset);
		expected = (73.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 1;
		offset = 15;
		actual = technicals.sma(stock, period, offset);
		expected = (24.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 3;
		offset = 2;
		actual = technicals.sma(stock, period, offset);
		expected = (9.0 + 23.0 + 46.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 3;
		offset = 7;
		actual = technicals.sma(stock, period, offset);
		expected = (29.0 + 33.0 + 55.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 3;
		offset = 15;
		actual = technicals.sma(stock, period, offset);
		expected = (52.0 + 21.0 + 24.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 15;
		offset = 14;
		actual = technicals.sma(stock, period, offset);
		expected = (9.0 + 23.0 + 46.0 + 35.0 + 14.0 + 29.0 + 33.0 + 55.0 + 73.0 + 11.0 + 28.0 + 41.0 + 79.0 + 52.0 + 21.0) / period;
		assertThat(actual).isEqualTo(expected);
		
		period = 15;
		offset = 15;
		actual = technicals.sma(stock, period, offset);
		expected = (23.0 + 46.0 + 35.0 + 14.0 + 29.0 + 33.0 + 55.0 + 73.0 + 11.0 + 28.0 + 41.0 + 79.0 + 52.0 + 21.0 + 24.0) / period;
		assertThat(actual).isEqualTo(expected);
	}
	
	@Test
	public void testEma() {
		int period;
		int offset;
		double actual;
		double expected;
		Stock stock = new Stock("ABCD", historicalPrices);
		
		period = 1;
		offset = 0;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 9.00;
		assertThat(actual).isEqualTo(expected);
		
		period = 1;
		offset = 1;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 23.00;
		assertThat(actual).isEqualTo(expected);
		
		period = 10;
		offset = 9;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 32.80;
		assertThat(actual).isEqualTo(expected);
		
		period = 10;
		offset = 10;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 31.93;
		assertThat(actual).isEqualTo(expected);
		
		period = 10;
		offset = 11;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 33.58;
		assertThat(actual).isEqualTo(expected);
		
		period = 10;
		offset = 12;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 41.84;
		assertThat(actual).isEqualTo(expected);
		
		period = 15;
		offset = 14;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 36.60;
		assertThat(actual).isEqualTo(expected);
		
		period = 15;
		offset = 15;
		actual = Math.round(technicals.ema(stock, period, offset) * 100.0) / 100.0;
		expected = 35.03;
		assertThat(actual).isEqualTo(expected);
	}
	
	@Test
	public void testRsi() {
		int period;
		int offset;
		double actual;
		double expected;
		Stock stock = new Stock("ABCD", historicalPrices);
		
		period = 14;
		offset = 14;
		actual = Math.round(technicals.rsi(stock, period, offset) * 100.0) / 100.0;
		expected = 51.90;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 15;
		actual = Math.round(technicals.rsi(stock, period, offset) * 100.0) / 100.0;
		expected = 52.39;
		
		assertThat(actual).isEqualTo(expected);
	}
	
	@Test
	public void testBollingerBands() {
		int period;
		int offset;
		double actual;
		double expected;
		Stock stock = new Stock("ABCD", historicalPrices);
		
		period = 14;
		offset = 13;
		actual = Math.round(technicals.bollingerDown(stock, period, offset) * 100.0) / 100.0;
		expected = -3.86;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 13;
		actual = Math.round(technicals.bollingerUp(stock, period, offset) * 100.0) / 100.0;
		expected = 79.29;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 14;
		actual = Math.round(technicals.bollingerDown(stock, period, offset) * 100.0) / 100.0;
		expected = -1.05;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 14;
		actual = Math.round(technicals.bollingerUp(stock, period, offset) * 100.0) / 100.0;
		expected = 78.19;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 15;
		actual = Math.round(technicals.bollingerDown(stock, period, offset) * 100.0) / 100.0;
		expected = -0.87;
		assertThat(actual).isEqualTo(expected);
		
		period = 14;
		offset = 15;
		actual = Math.round(technicals.bollingerUp(stock, period, offset) * 100.0) / 100.0;
		expected = 78.15;
		assertThat(actual).isEqualTo(expected);
	}
}
