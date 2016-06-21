package com.k12systems.jdbcutils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerDataSource extends BaseDataSource implements javax.sql.DataSource {
	public DriverManagerDataSource(String connectUri) {
		this(connectUri,null);
	}
	
	public DriverManagerDataSource(String connectUri, Properties props) {
		super(connectUri, props);
	}

	@Override
	protected Connection connect(String url, Properties p) throws SQLException {
		if(null == p) return DriverManager.getConnection(url);
		else return DriverManager.getConnection(url, p);
	}
}
