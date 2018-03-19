package com.stocks.analysis.data;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Handles database operations
 * @author momintariq
 *
 */
@Component
public class HistoricalPricesJdbcTemplate {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * Retrieves a list of table names
	 * @return list of table names
	 * @throws SQLException
	 */
	public List<String> getTableNames() throws SQLException {
		DatabaseMetaData metadata = jdbcTemplate.getDataSource().getConnection().getMetaData();
		ResultSet rs = metadata.getTables(null, null, "%", null);
		List<String> tables = new ArrayList<String>();
		while(rs.next()) {
			String table = rs.getString(3);
			tables.add(table);
			logger.info("Retrieved table: {}", table);
		}
		return tables;
	}
	
	/**
	 * Creates a table
	 * @param ticker
	 */
	public void createTable(final Stock stock) {
		String ticker = stock.getTicker();
		String sql = Sql.CREATE_TABLE;
		String tableName = ticker + Sql.TABLE_NAME_SUFFIX;
		sql = sql.replace(Sql.TABLE_NAME_PLACEHOLDER, tableName);
		jdbcTemplate.execute(sql);
		logger.info("Created table: {}", tableName);
	}
	
	/**
	 * Deletes a table
	 * @param ticker
	 */
	public void deleteTable(final String tableName) {
		String sql = Sql.DELETE_TABLE;
		sql = sql.replace(Sql.TABLE_NAME_PLACEHOLDER, tableName);
		jdbcTemplate.execute(sql);
		logger.info("Deleted table: {}", tableName);
	}
	
	/**
	 * Deletes all tables
	 * @throws SQLException
	 */
	public void cleanDatabase() throws SQLException {
		getTableNames().forEach(table -> {
			deleteTable(table);
		});
		logger.info("Cleaned database");
	}
	
	/**
	 * Insert historical prices for a stock
	 * @param stock containing historical prices
	 */
	public void insertHistoricalPrices(final Stock stock) {
		String ticker = stock.getTicker();
		List<HistoricalPrices> historicalPrices = stock.getHistoricalPrices();
		String sql = Sql.INSERT_HISTORICAL_PRICES;
		String tableName = ticker + Sql.TABLE_NAME_SUFFIX;
		sql = sql.replace(Sql.TABLE_NAME_PLACEHOLDER, tableName);
		List<Object[]> historicalPricesParams = new ArrayList<Object[]>();
		historicalPrices.forEach(hp -> {
			historicalPricesParams.add(new Object[] { hp.getDate(), hp.getOpen(), hp.getHigh(), hp.getLow(), hp.getClose(), hp.getVolume() });
		});
		jdbcTemplate.batchUpdate(sql, historicalPricesParams);
		logger.info("Persisted {} historical prices for {}", historicalPrices.size(), ticker);
	}
	
	/**
	 * Query and return historical prices for a ticker
	 * @param ticker
	 * @return list of historical prices
	 */
	public List<HistoricalPrices> getHistoricalPrices(final String ticker) {
		String sql = Sql.GET_STOCK;
		String tableName = ticker + Sql.TABLE_NAME_SUFFIX;
		sql = sql.replace(Sql.TABLE_NAME_PLACEHOLDER, tableName);
		List<HistoricalPrices> historicalPrices = jdbcTemplate.query(sql, new HistoricalPricesRowMapper());
		logger.info("Retrieved historical prices for {}", ticker);
		return historicalPrices;
	}
}
