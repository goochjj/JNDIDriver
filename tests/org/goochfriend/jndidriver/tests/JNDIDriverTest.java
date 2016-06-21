package org.goochfriend.jndidriver.tests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.naming.java.javaURLContextFactory;
import org.goochfriend.jndidriver.JNDIDriver;
import org.junit.BeforeClass;
import org.junit.Test;

import com.k12systems.jdbcutils.DriverManagerDataSource;

import static org.junit.Assert.*;

public class JNDIDriverTest {
	public static Properties dbProps;
	
	@BeforeClass
	public static void getDBProps() throws Exception {
		try {
			Properties p = new Properties();
			p.load(JNDIDriverTest.class.getResourceAsStream("db.properties"));
			dbProps = p;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Please populate db.properties with a testable datasource");
		}
	}
	
	@BeforeClass
	public static void setupJNDI() throws Exception {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		
		InitialContext ic = new InitialContext();
		ic.createSubcontext("java:");
		ic.createSubcontext("java:/comp");
		ic.createSubcontext("java:/comp/env");
		ic.createSubcontext("java:/comp/env/jdbc");
	}
	@BeforeClass
	public static void registerDS() throws Exception {
		Context ctx = new InitialContext();
		Properties prop = new Properties();
		prop.setProperty("user", dbProps.getProperty("user"));
		prop.setProperty("password", dbProps.getProperty("password"));
		DriverManagerDataSource ds = new DriverManagerDataSource(dbProps.getProperty("url"),prop);
		{
			Connection con = ds.getConnection();
			assertNotNull(con);
			con.close();
		}
		ctx.bind("java:/comp/env/jdbc/test", ds);
		{
			DataSource ds2 = (DataSource)ctx.lookup("java:/comp/env/jdbc/test");
			Connection con=ds2.getConnection();
			assertNotNull(con);
			con.close();
		}
	}
	@BeforeClass
	public static void registerDSNoLogin() throws Exception {
		Context ctx = new InitialContext();
		DriverManagerDataSource ds = new DriverManagerDataSource(dbProps.getProperty("url"));
		ctx.bind("java:/comp/env/jdbc/testNoLogin", ds);
	}	
	@Test
	public void testDriverRegistration() throws Exception {
		DriverManager.registerDriver(new JNDIDriver());
		assertNotNull(DriverManager.getDriver("java:jndi:TestURL"));
	}
	
	@Test
	public void testSPIDriverRegistration() throws Exception {
		assertNotNull(DriverManager.getDriver("java:jndi:TestURL"));
	}

	@Test
	public void testJNDIDS() throws Exception {
		DriverManager.registerDriver(new JNDIDriver());
		Connection con = DriverManager.getConnection("java:jndi:java:/comp/env/jdbc/test");
		assertNotNull(con);
	}

	@Test
	public void testJNDIDSWithLogin() throws Exception {
		DriverManager.registerDriver(new JNDIDriver());
		//DriverManager.setLogWriter(new java.io.PrintWriter(System.err));
		Connection con = DriverManager.getConnection("java:jndi:java:/comp/env/jdbc/testNoLogin", "mrwizard", "hln02sound!");
		assertNotNull(con);
	}
}
