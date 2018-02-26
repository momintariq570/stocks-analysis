package com.stocks.analysis.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Maps a row from the result set to
 * a HistoricalPrices object
 * @author momintariq
 *
 */
public class HistoricalPricesRowMapper implements RowMapper<HistoricalPrices> {

	/**
	 * Maps a result set row and returns
	 * a HistoricalPrices object 
	 */
	@Override
	public HistoricalPrices mapRow(ResultSet rs, int rowNum) throws SQLException {
		String date = rs.getString(Sql.DATE);
		double open = rs.getDouble(Sql.OPEN);
		double high = rs.getDouble(Sql.HIGH);
		double low = rs.getDouble(Sql.LOW);
		double close = rs.getDouble(Sql.CLOSE);
		int volume = rs.getInt(Sql.VOLUME);
		return new HistoricalPrices(date, open, high, low, close, volume);
	}
}
