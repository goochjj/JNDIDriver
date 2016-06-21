package org.goochfriend.jndidriver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class JNDIDriver implements Driver {
	public Connection connect(String url, Properties info) throws SQLException {
		InitialContext ctx = null;
		try {
			 ctx = new InitialContext();
		} catch (Exception ex){
			throw new SQLException("Naming Exception",ex);
		}
		try {
			String find = url;
			if (find.startsWith("java:jndi:")) find=find.substring(10);
			Object o = ctx.lookup(find);
			if (o instanceof java.sql.Connection) {
				return (java.sql.Connection)o;
			}
			if (o instanceof DataSource) {
				String user = info.getProperty("user");
				String pw = info.getProperty("password");
				if (user!=null || pw!=null) {
					return ((DataSource)o).getConnection(user, pw);
				} else {
					return ((DataSource)o).getConnection();
				}
			}
			throw new SQLException("Object returned of class "+o.getClass().getName()+" could not be handled - need DataSource");
		} catch (NamingException e) {
			throw new SQLException("JNDI Naming Exception", e);
		} finally {
			if (ctx!=null) {
				try { ctx.close(); }
				catch (NamingException e) {
					e.printStackTrace();
				}
				ctx = null;
			}
		}
	}

	public boolean acceptsURL(String url) throws SQLException {
		if (url.startsWith("java:jndi:")) return true;
		return false;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException { return null; }
	public int getMajorVersion() { return 1; }
	public int getMinorVersion() { return 0; }
	public boolean jdbcCompliant() { return true; }

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

}
