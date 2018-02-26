package com.stocks.analysis;

import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import com.stocks.analysis.data.HistoricalPrices;
import com.stocks.analysis.data.HistoricalPricesJdbcTemplate;
import com.stocks.analysis.data.Stock;
import com.stocks.analysis.data.StockAnalysisService;
import com.stocks.analysis.data.Technicals;
import com.stocks.analysis.data.Technicals.TechnicalType;
import com.stocks.analysis.data.Trade;

@SpringBootApplication
public class Application {
	
	@Autowired
	private HistoricalPricesJdbcTemplate historicalPricesJdbcTemplate;
	
	@Autowired
	private StockAnalysisService stockAnalysisService;
	
	@Autowired
	private Technicals technicals;
	
	private double initialBalance = 25000;
	private HashMap<String, Trade> trades = new HashMap<String, Trade>();
	private TechnicalType[] technicalTypes = new TechnicalType[] {
			TechnicalType.EMA,
			TechnicalType.RSI
	};

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	@Order(1)
	public CommandLineRunner commandLineRunner() {
		return args -> {
			/*String ticker = "ATVI";
			runBackTest(ticker);
			strategyResults(trades);*/
			
			
			String[] tickers = new String[] {"ATVI", "AAPL"};
			historicalPricesJdbcTemplate.cleanDatabase();
			/*Arrays.asList(tickers).forEach(ticker -> {
				try {
					stockAnalysisService.getHistoricalPrices(ticker);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Stock stock = new Stock("AAPL", historicalPricesJdbcTemplate.getHistoricalPrices(ticker));
				double ema = technicals.ema(stock, 50, stock.getHistoricalPrices().size() - 1);
				double sma = technicals.sma(stock, 50, stock.getHistoricalPrices().size() - 1);
				double rsi = technicals.rsi(stock, 14, stock.getHistoricalPrices().size() - 1);
				double bollDown = technicals.bollingerDown(stock, 50, stock.getHistoricalPrices().size() - 1);
				double bollUp = technicals.bollingerUp(stock, 50, stock.getHistoricalPrices().size() - 1);
				double macd = technicals.macd(stock, stock.getHistoricalPrices().size() - 1);
				System.out.println("EMA: " + ema);
				System.out.println("SMA: " + sma);
				System.out.println("RSI: " + rsi);
				System.out.println("BOLL-DOWN: " + bollDown);
				System.out.println("BOLL-UP: " + bollUp);
				System.out.println("MACD: " + macd);
			});*/
		};
	}
	
	private void runBackTest(String ticker) throws Exception {
		double balance = initialBalance;
		double cash = balance;
		int shares = 0;
		int buyIndicators = 0;

		historicalPricesJdbcTemplate.cleanDatabase();
		stockAnalysisService.getHistoricalPrices(ticker);
		Stock stock = new Stock(ticker, historicalPricesJdbcTemplate.getHistoricalPrices(ticker));
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		boolean inTrade = false;
		String entryTradeDate = "";
		
		PrintWriter pw = new PrintWriter(new File(ticker + "-backtest.csv"));
		StringBuilder sb = new StringBuilder();
		sb.append("date");
		sb.append(',');
		sb.append("priceOpen");
		sb.append(",");
		sb.append("priceClose");
		sb.append(",");
		
		for(int i = 0; i < technicalTypes.length; i++) {
			if(technicalTypes[i] == TechnicalType.RSI) {
				sb.append("rsi");
				sb.append(",");
			}
			if(technicalTypes[i] == TechnicalType.EMA) {
				sb.append("ema");
				sb.append(",");
			}
		}
		
		for(int i = 0; i < technicalTypes.length; i++) {
			if(technicalTypes[i] == TechnicalType.RSI) {
				sb.append("buy/sell (rsi)");
				sb.append(",");
			}
			if(technicalTypes[i] == TechnicalType.EMA) {
				sb.append("buy/sell (ema)");
				sb.append(",");
			}
		}
		
		sb.append("trade");
		sb.append("\n");
		pw.write(sb.toString());
		sb.setLength(0);
		
		for (int i = 75; i < historicalPrices.size(); i++) {
			String dateTomorrow = "";
			double openPriceTomorrow = 0;
			String dateToday = historicalPrices.get(i).getDate();
			double openPriceToday = historicalPrices.get(i).getOpen();
			double closePriceToday = historicalPrices.get(i).getClose();
			if (i != historicalPrices.size() - 1) {
				dateTomorrow = historicalPrices.get(i + 1).getDate();
				openPriceTomorrow = historicalPrices.get(i + 1).getOpen();
			}
			
			sb.append(dateToday);
			sb.append(",");
			sb.append(openPriceToday);
			sb.append(",");
			sb.append(closePriceToday);
			sb.append(",");
			
			double ema = 0;
			double rsi = 0;
			
			for(int j = 0; j < technicalTypes.length; j++) {
				if(technicalTypes[j] == TechnicalType.RSI) {
					rsi = technicals.rsi(stock, 14, i);
					sb.append(rsi);
					sb.append(",");
				}
				if(technicalTypes[j] == TechnicalType.EMA) {
					ema = technicals.ema(stock, 50, i);
					sb.append(ema);
					sb.append(",");
				}
			}
			
			for(int j = 0; j < technicalTypes.length; j++) {
				if(technicalTypes[j] == TechnicalType.EMA) {
					if(closePriceToday < ema) {
						sb.append("SELL");
						sb.append(",");
					} else {
						sb.append("BUY");
						sb.append(",");
						buyIndicators++;
					}
				}
				if(technicalTypes[j] == TechnicalType.RSI) {
					if (rsi < 50) {
						sb.append("SELL");
						sb.append(",");
					} else {
						sb.append("BUY");
						sb.append(",");
						buyIndicators++;
					}
				}
			}
			
			// Buy
			if (!inTrade && buyIndicators == technicalTypes.length) {
				shares = (int) (balance / openPriceTomorrow);
				cash = balance - (shares * openPriceTomorrow);
				System.out.printf(dateTomorrow + ": BUY (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
				trades.put(dateTomorrow, new Trade(dateTomorrow, openPriceTomorrow, openPriceTomorrow));
				entryTradeDate = dateTomorrow;
				inTrade = true;
				sb.append("Buy Signal");
			}
			
			// Update max price
			if (inTrade && trades.get(entryTradeDate).getMaxPrice() < closePriceToday) {
				trades.get(entryTradeDate).setMaxPrice(closePriceToday);
			}
			
			// Sell
			if (inTrade && buyIndicators == 0) {
				double proceeds = shares * openPriceTomorrow;
				balance = cash + proceeds;
				cash = balance;
				System.out.printf(dateTomorrow + ": SELL (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
				trades.get(entryTradeDate).setExitDate(dateTomorrow);
				trades.get(entryTradeDate).setExitPrice(openPriceTomorrow);
				inTrade = false;
				if (trades.get(entryTradeDate).getEntryPrice() < trades.get(entryTradeDate).getExitPrice()) {
					trades.get(entryTradeDate).setProfitable(true);
				} else {
					trades.get(entryTradeDate).setProfitable(false);
				}
				sb.append("Sell Signal");
			}

			sb.append("\n");
			pw.write(sb.toString());
			sb.setLength(0);
			buyIndicators = 0;
		}
		pw.close();
		if (inTrade) {
			System.out.printf("Current balance: %.2f\n",
					(shares * historicalPrices.get(historicalPrices.size() - 1).getClose()) + cash);
		} else {
			System.out.printf("Current balance: %.2f\n", balance);
		}
	}
	
	private void strategyResults(HashMap<String, Trade> trades) {
		double winTrades = 0;
		double lossTrades = 0;
		ArrayList<Double> winReturns = new ArrayList<Double>();
		ArrayList<Double> lossReturns = new ArrayList<Double>();
		double winReturnSum = 0;
		double lossReturnSum = 0;
		int totalTrades = trades.keySet().size();
		for (String entryTradeDate : trades.keySet()) {
			if (trades.get(entryTradeDate).isProfitable()) {
				winTrades++;
				winReturns.add(
						(trades.get(entryTradeDate).getExitPrice() / trades.get(entryTradeDate).getEntryPrice()) - 1);
			} else {
				if (trades.get(entryTradeDate).getExitPrice() != 0) {
					lossTrades++;
					lossReturns.add(
							(trades.get(entryTradeDate).getExitPrice() / trades.get(entryTradeDate).getEntryPrice())
									- 1);
				}
			}
		}

		Collections.sort(winReturns);
		Collections.sort(lossReturns);

		for (int i = 1; i < winReturns.size() - 1; i++) {
			winReturnSum += winReturns.get(i);
		}
		for (int i = 1; i < lossReturns.size() - 1; i++) {
			lossReturnSum += lossReturns.get(i);
		}

		double winMedian = 0;
		double lossMedian = 0;
		if (winReturns.size() % 2 == 0) {
			double first = winReturns.get((winReturns.size() / 2) - 1);
			double second = winReturns.get(winReturns.size() / 2);
			winMedian = (first + second) / 2;
		} else {
			winMedian = winReturns.get((winReturns.size() - 1) / 2);
		}

		if (lossReturns.size() % 2 == 0) {
			double first = lossReturns.get((lossReturns.size() / 2) - 1);
			double second = lossReturns.get(lossReturns.size() / 2);
			lossMedian = (first + second) / 2;
		} else {
			lossMedian = lossReturns.get((lossReturns.size() - 1) / 2);
		}
		System.out.println();
		System.out.println("Total trades: " + totalTrades);
		System.out.println("Winning trades: " + (int) winTrades);
		System.out.printf("Winning percentage: %.2f%%\n", (winTrades / totalTrades) * 100);
		System.out.println("----------------------------------------");
		System.out.printf("Largest winning trade return: %.2f%%\n", winReturns.get(winReturns.size() - 1) * 100);
		System.out.printf("Largest losing trade return: %.2f%%\n", lossReturns.get(0) * 100);
		System.out.println("----------------------------------------");
		System.out.printf("Smallest winning trade return: %.2f%%\n", winReturns.get(0) * 100);
		System.out.printf("Smallest losing trade return: %.2f%%\n", lossReturns.get(lossReturns.size() - 1) * 100);
		System.out.println("----------------------------------------");
		System.out.printf("Average winning trade return: %.2f%%\n", (winReturnSum / (winReturns.size() - 2)) * 100);
		System.out.printf("Average losing trade return: %.2f%%\n", (lossReturnSum / (lossReturns.size() - 2)) * 100);
		System.out.println("----------------------------------------");
		System.out.printf("Median winning trade return: %.2f%%\n", winMedian * 100);
		System.out.printf("Median losing trade return: %.2f%%\n", lossMedian * 100);
	}
}
