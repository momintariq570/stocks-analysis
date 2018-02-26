package com.stocks.analysis.data;

/**
 * Sql statements and metadata information
 * @author momintariq
 *
 */
public class Sql {

	public static final String DATE = "date";
	
	public static final String OPEN = "open";
	
	public static final String HIGH = "high";
	
	public static final String LOW = "low";
	
	public static final String CLOSE = "close";
	
	public static final String VOLUME = "volume";
	
	public static final String TABLE_NAME_PLACEHOLDER = "table_name";
	
	public static final String TABLE_NAME_SUFFIX = "_historical_prices";
	
	public static final String CREATE_TABLE = "create table " + TABLE_NAME_PLACEHOLDER + " ("
			+ "id integer primary key, "
			+ DATE + " text not null, "
			+ OPEN + " float not null, "
    			+ HIGH + " float not null, "
    			+ LOW + " float not null, "
    			+ CLOSE + " float not null, "
    			+ VOLUME + " integer not null"
			+ ");";
	
	public static final String DELETE_TABLE = "drop table " + TABLE_NAME_PLACEHOLDER + ";";
	
	public static final String INSERT_HISTORICAL_PRICES = "insert into " + TABLE_NAME_PLACEHOLDER + " (" + DATE + ", " + OPEN + ", " + HIGH + ", " + LOW + ", " + CLOSE + ", " + VOLUME + ") values (?, ?, ?, ?, ?, ?);";
	
	public static final String GET_STOCK = "select * from " + TABLE_NAME_PLACEHOLDER + " order by " + DATE + " asc;";
}
