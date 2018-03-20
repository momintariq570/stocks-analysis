package com.stocks.analysis;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.stocks.analysis.data.HistoricalPrices;
import com.stocks.analysis.data.HistoricalPricesJdbcTemplate;
import com.stocks.analysis.data.Stock;
import com.stocks.analysis.data.StockAnalysisService;
import com.stocks.analysis.data.Technicals;
import com.stocks.analysis.data.Trade;
import com.stocks.analysis.data.Technicals.TechnicalType;

@Component
public class StockServiceCommandLineRunner implements CommandLineRunner {
	
	@Autowired
	private HistoricalPricesJdbcTemplate historicalPricesJdbcTemplate;
	
	@Autowired
	private StockAnalysisService stockAnalysisService;
	
	@Autowired
	private Technicals technicals;
	
	@Value("${download.data.num.threads}")
	private int numThreads;
	
	private double initialBalance;
	private HashMap<String, Trade> trades;
	private TechnicalType[] technicalTypes;
	private ExecutorService downloadDataExecutorService;
	private List<Stock> stocks;
	private final String OUTPUT_FOLDER = "backtest-results";

	@PostConstruct
	public void setup() {
		initialBalance = 25000;
		trades = new HashMap<String, Trade>();
		technicalTypes = new TechnicalType[] {TechnicalType.EMA, TechnicalType.RSI, TechnicalType.CCI};
		downloadDataExecutorService = Executors.newFixedThreadPool(numThreads);
		stocks = new ArrayList<Stock>();
	}
	
	@Override
	public void run(String... args) throws Exception {		
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String[] tickers = new String[] {"AAPL"};
		List<Future<?>> futures = new ArrayList<Future<?>>();
		Arrays.asList(tickers).forEach(ticker -> {
			Future<?> future = downloadDataExecutorService.submit(downloadDataFutureTask(ticker));
			futures.add(future);
		});
		
		for(Future<?> future : futures) {
			future.get();
		}
		
		stocks.forEach(stock -> {
			try {
				// runBackTest(stock);
			    emaTest(stock);
				strategyResults(trades);
				trades.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		/*
		historicalPricesJdbcTemplate.cleanDatabase();
		stocks.forEach(stock -> {
			stockAnalysisService.saveHistoricalPrices(stock);
		});
		*/
		stopWatch.stop();
		System.out.println("Execution took " + stopWatch.getTotalTimeSeconds() + " seconds");
		downloadDataExecutorService.shutdownNow();
	}
	
	private FutureTask<Stock> downloadDataFutureTask(String ticker) {
		FutureTask<Stock> futureTask = new FutureTask<Stock>(new Runnable() {
			@Override
			public void run() {
				try {
					List<HistoricalPrices> historicalPrices = stockAnalysisService.downloadHistoricalPrices(ticker);
					Collections.reverse(historicalPrices);
					Stock stock = new Stock(ticker, historicalPrices);
					stocks.add(stock);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, null);
		return futureTask;
	}
	
	private void runBackTest(Stock stock) throws Exception {
		double balance = initialBalance;
		double cash = balance;
		int shares = 0;
		int buyIndicators = 0;

		String ticker = stock.getTicker();
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		
		boolean inTrade = false;
		String entryTradeDate = "";
		
		PrintWriter pw = new PrintWriter(new File(OUTPUT_FOLDER + "/" + ticker + "-backtest.csv"));
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
			if(technicalTypes[i] == TechnicalType.CCI) {
				sb.append("cci");
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
			if(technicalTypes[i] == TechnicalType.CCI) {
				sb.append("buy/sell (cci)");
				sb.append(",");
			}
		}
		
		sb.append("returns");
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
			double cci = 0;
			
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
				if(technicalTypes[j] == TechnicalType.CCI) {
					cci = technicals.cci(stock, 40, i);
					sb.append(cci);
					sb.append(",");
				}
			}
			
			for(int j = 0; j < technicalTypes.length; j++) {
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
				if(technicalTypes[j] == TechnicalType.CCI) {
					if (cci < 0) {
						sb.append("SELL");
						sb.append(",");
					} else {
						sb.append("BUY");
						sb.append(",");
						buyIndicators++;
					}
				}
			}
			
			if(inTrade) {
				if(buyIndicators < technicalTypes.length) {
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
				}
				sb.append(getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday));
				sb.append(",");
			} else {
				if(buyIndicators == technicalTypes.length) {
					shares = (int) (balance / openPriceTomorrow);
					cash = balance - (shares * openPriceTomorrow);
					System.out.printf(dateTomorrow + ": BUY (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
					trades.put(dateTomorrow, new Trade(dateTomorrow, openPriceTomorrow, openPriceTomorrow));
					entryTradeDate = dateTomorrow;
					inTrade = true;
				}
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
	
	private void emaTest(Stock stock) throws Exception {
	    double balance = initialBalance;
        double cash = balance;
        int shares = 0;

        String ticker = stock.getTicker();
        List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
        
        boolean inTrade = false;
        String entryTradeDate = "";
        
        PrintWriter pw = new PrintWriter(new File(OUTPUT_FOLDER + "/" + ticker + "-ema-backtest.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("date");
        sb.append(',');
        sb.append("priceOpen");
        sb.append(",");
        sb.append("priceClose");
        sb.append(",");
        sb.append("ema");
        sb.append(",");
        sb.append("buy/sell (ema)");
        sb.append(",");
        sb.append("returns");
        sb.append("\n");
        pw.write(sb.toString());
        sb.setLength(0);
		
		for (int i = 75; i < historicalPrices.size(); i++) {
			String dateTomorrow = "";
			double openPriceTomorrow = 0;
			String dateToday = historicalPrices.get(i).getDate();
			double openPriceToday = historicalPrices.get(i).getOpen();
			double closePriceToday = historicalPrices.get(i).getClose();
			double closePriceYesterday = historicalPrices.get(i - 1).getClose();
			if (i != historicalPrices.size() - 1) {
				dateTomorrow = historicalPrices.get(i + 1).getDate();
				openPriceTomorrow = historicalPrices.get(i + 1).getOpen();
			}
			
			double emaYesterday = technicals.ema(stock, 50, i - 1);
			double emaToday = technicals.ema(stock, 50, i);
			
			sb.append(dateToday);
            sb.append(",");
            sb.append(openPriceToday);
            sb.append(",");
            sb.append(closePriceToday);
            sb.append(",");
            sb.append(emaToday);
            sb.append(",");
			
			if(!inTrade) {
			    if(closePriceYesterday < emaYesterday && closePriceToday > emaToday) {
			        // buy
	                shares = (int)Math.floor(balance / openPriceTomorrow);
	                cash = balance - (shares * openPriceTomorrow);
	                System.out.printf(dateTomorrow + ": BUY (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
	                trades.put(dateTomorrow, new Trade(dateTomorrow, openPriceTomorrow, openPriceTomorrow));
	                entryTradeDate = dateTomorrow;
	                inTrade = true;
	                sb.append("BUY");
	                sb.append(",");
			    } else {
			        sb.append("SELL");
                    sb.append(",");
			    }
			} else {
			    if(trades.get(entryTradeDate).getMaxPrice() < closePriceToday) {
			        // Update max price
	                trades.get(entryTradeDate).setMaxPrice(closePriceToday);    
			    }
			    if((closePriceYesterday > emaYesterday && closePriceToday < emaToday)) {
			        // sell
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
                    sb.append("SELL");
                    sb.append(",");
			    } else {
			        sb.append("BUY");
                    sb.append(",");
                    sb.append(getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday));
                    sb.append(",");
			    }
			}
			sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
		}
		
		pw.close();
		
		if (inTrade) {
			System.out.printf("Current balance: %.2f\n",
					(shares * historicalPrices.get(historicalPrices.size() - 1).getClose()) + cash);
		} else {
			System.out.printf("Current balance: %.2f\n", balance);
		}
	}
	
	private void rsiTest(Stock stock) throws Exception {
	    double balance = initialBalance;
        double cash = balance;
        int shares = 0;

        String ticker = stock.getTicker();
        List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
        
        boolean inTrade = false;
        String entryTradeDate = "";
        
        PrintWriter pw = new PrintWriter(new File(OUTPUT_FOLDER + "/" + ticker + "-rsi-backtest.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("date");
        sb.append(',');
        sb.append("priceOpen");
        sb.append(",");
        sb.append("priceClose");
        sb.append(",");
        sb.append("rsi");
        sb.append(",");
        sb.append("buy/sell (rsi)");
        sb.append(",");
        sb.append("returns");
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
			
			double rsiYesterday = technicals.rsi(stock, 14, i - 1);
			double rsiToday = technicals.rsi(stock, 14, i);
			
			sb.append(dateToday);
            sb.append(",");
            sb.append(openPriceToday);
            sb.append(",");
            sb.append(closePriceToday);
            sb.append(",");
            sb.append(rsiToday);
            sb.append(",");
			
			if(!inTrade) {
			    if(rsiYesterday < 50 && rsiToday >= 50) {
			        // buy
	                shares = (int)Math.floor(balance / openPriceTomorrow);
	                cash = balance - (shares * openPriceTomorrow);
	                System.out.printf(dateTomorrow + ": BUY (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
	                trades.put(dateTomorrow, new Trade(dateTomorrow, openPriceTomorrow, openPriceTomorrow));
	                entryTradeDate = dateTomorrow;
	                inTrade = true;
	                sb.append("BUY");
	                sb.append(",");
			    } else {
			        sb.append("SELL");
                    sb.append(",");
			    }
			} else {
			    if(trades.get(entryTradeDate).getMaxPrice() < closePriceToday) {
			        // Update max price
	                trades.get(entryTradeDate).setMaxPrice(closePriceToday);    
			    }
			    if((rsiYesterday >= 50 && rsiToday < 50) || (getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday) < -.05)) {
			        // sell
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
                    sb.append("SELL");
                    sb.append(",");
			    } else {
			        sb.append("BUY");
                    sb.append(",");
                    sb.append(getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday));
                    sb.append(",");
			    }
			}
			sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
		}
		
		pw.close();
		
		if (inTrade) {
			System.out.printf("Current balance: %.2f\n",
					(shares * historicalPrices.get(historicalPrices.size() - 1).getClose()) + cash);
		} else {
			System.out.printf("Current balance: %.2f\n", balance);
		}
	}
	
	private void cciTest(Stock stock) throws Exception {
	    double balance = initialBalance;
        double cash = balance;
        int shares = 0;

        String ticker = stock.getTicker();
        List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
        
        boolean inTrade = false;
        String entryTradeDate = "";
        
        PrintWriter pw = new PrintWriter(new File(OUTPUT_FOLDER + "/" + ticker + "-cci-backtest.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("date");
        sb.append(',');
        sb.append("priceOpen");
        sb.append(",");
        sb.append("priceClose");
        sb.append(",");
        sb.append("cci");
        sb.append(",");
        sb.append("buy/sell (cci)");
        sb.append(",");
        sb.append("returns");
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
            
            double cciYesterday = technicals.cci(stock, 40, i - 1);
            double cciToday = technicals.cci(stock, 40, i);
            
            sb.append(dateToday);
            sb.append(",");
            sb.append(openPriceToday);
            sb.append(",");
            sb.append(closePriceToday);
            sb.append(",");
            sb.append(cciToday);
            sb.append(",");
            
            if(!inTrade) {
                if(cciYesterday <= 0 && cciToday > 0) {
                    // buy
                    shares = (int)Math.floor(balance / openPriceTomorrow);
                    cash = balance - (shares * openPriceTomorrow);
                    System.out.printf(dateTomorrow + ": BUY (%.2f) balance: %.2f\n", openPriceTomorrow, balance);
                    trades.put(dateTomorrow, new Trade(dateTomorrow, openPriceTomorrow, openPriceTomorrow));
                    entryTradeDate = dateTomorrow;
                    inTrade = true;
                    sb.append("BUY");
                    sb.append(",");
                } else {
                    sb.append("SELL");
                    sb.append(",");
                }
            } else {
                if(trades.get(entryTradeDate).getMaxPrice() < closePriceToday) {
                    // Update max price
                    trades.get(entryTradeDate).setMaxPrice(closePriceToday);    
                }
                if((cciYesterday >= 0 && cciToday < 0) || (getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday) < -.05)) {
                    // sell
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
                    sb.append("SELL");
                    sb.append(",");
                } else {
                    sb.append("BUY");
                    sb.append(",");
                    sb.append(getChange(trades.get(entryTradeDate).getEntryPrice(), closePriceToday));
                    sb.append(",");
                }
            }
            sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
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
	
	private double getChange(double start, double end) {
		return ((end / start) - 1);
	}
}
