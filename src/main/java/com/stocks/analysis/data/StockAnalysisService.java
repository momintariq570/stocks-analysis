package com.stocks.analysis.data;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Retrieves various financial information
 * for a given stock from the internet
 * @author momintariq
 *
 */
@Component
public class StockAnalysisService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HistoricalPricesJdbcTemplate historicalPricesJdbcTemplate;
	
	@Value("${quandl.historical.prices.url}")
	private String quandlBaseUrl;
	
	@Value("${quandl.api.key}")
	private String quandlApiKey;
	
	/**
	 * Downloads historical prices from Quandl for a given ticker
	 * @param ticker
	 * @return list of historical prices
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws JSONException 
	 */
	public List<HistoricalPrices> downloadHistoricalPrices(final String ticker) throws MalformedURLException, URISyntaxException, JSONException {
		URL url = new URL(quandlBaseUrl.replace("%ticker%", ticker) + quandlApiKey);
		RestTemplate restTemplate = new RestTemplate();
		String line = restTemplate.getForObject(url.toURI(), String.class);
		JSONObject jsonObj = new JSONObject(line);
		JSONArray dataArr = jsonObj.getJSONObject("dataset_data").getJSONArray("data");
		List<HistoricalPrices> historicalPrices = new ArrayList<HistoricalPrices>();
		for (int i = 0; i < dataArr.length(); i++) {
			String date = dataArr.getJSONArray(i).getString(0);
			double open = dataArr.getJSONArray(i).getDouble(8);
			double high = dataArr.getJSONArray(i).getDouble(9);
			double low = dataArr.getJSONArray(i).getDouble(10);
			double close = dataArr.getJSONArray(i).getDouble(11);
			int volume = dataArr.getJSONArray(i).getInt(12);
			historicalPrices.add(new HistoricalPrices(date, open, high, low, close, volume));
		}
		logger.info("Downloaded {} historical prices for {}", historicalPrices.size(), ticker);
		return historicalPrices;
	}
	
	/**
	 * Saves historical prices for a stock
	 * @param stock
	 */
	public void saveHistoricalPrices(Stock stock) {
		historicalPricesJdbcTemplate.createTable(stock);
		historicalPricesJdbcTemplate.insertHistoricalPrices(stock);
	}
	
	/**
	 * Gets historical prices for a stock
	 * @param ticker
	 * @return
	 */
	public List<HistoricalPrices> getHistoricalPricesFromDb(String ticker) {
		return historicalPricesJdbcTemplate.getHistoricalPrices(ticker);
	}
}
