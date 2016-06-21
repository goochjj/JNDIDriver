package com.k12systems.jdbcutils;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

public abstract class BaseDataSource implements javax.sql.DataSource {
	protected final String _connectUri;
	protected Properties _props = null;

	public BaseDataSource(String connectUri) {
		this(connectUri,null);
	}

	public BaseDataSource(String connectUri, Properties props) {
		_connectUri = connectUri;
		_props = props;
	}

	public Properties getConnectionProperties() { return this._props; }
	public void setConnectionProperties(Properties p) { this._props = p; }

	protected abstract Connection connect(String uri, Properties props) throws SQLException;
	public Connection getConnection() throws SQLException {
		return connect(_connectUri,_props==null?new Properties():_props);
	}
	public Connection getConnection(String username, String password) throws SQLException {
		Properties p = new Properties();
		if (_props!=null) p.putAll(_props);
		if (username != null) {
			p.put("user", username);
		}
		if (password != null) {
			p.put("password", password);
		}
		System.err.println("BaseDS: connecting to "+_connectUri+" with "+p);
		return connect(_connectUri, p);
	}

	private PrintWriter logWriter = null;
	private int loginTimeout = 0;

	public PrintWriter getLogWriter() throws SQLException { return logWriter; }
	public void setLogWriter(PrintWriter out) throws SQLException { logWriter = out; }
	public int getLoginTimeout() throws SQLException { return loginTimeout; }
	public void setLoginTimeout(int seconds) throws SQLException { loginTimeout = seconds; }

	public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException { 
		if (iface.isInstance(this)) return (T)this; else return null; 
	}

	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { 
		throw new SQLFeatureNotSupportedException();
	}
}
