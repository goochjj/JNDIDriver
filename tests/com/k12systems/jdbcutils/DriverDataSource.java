package com.k12systems.jdbcutils;


import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class DriverDataSource extends BaseDataSource implements javax.sql.DataSource {
	protected final Driver _driver;

	public DriverDataSource(Driver drv, String connectUri) {
		this(drv, connectUri,null);
	}

	public DriverDataSource(Driver drv, String connectUri, Properties props) {
		super(connectUri, props);
		_driver = drv;
	}

	@Override
	protected Connection connect(String url, Properties p) throws SQLException {
		return _driver.connect(url,p);
	}
}
