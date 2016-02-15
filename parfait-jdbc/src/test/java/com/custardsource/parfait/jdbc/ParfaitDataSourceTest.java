package com.custardsource.parfait.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import com.custardsource.parfait.timing.ThreadMetric;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class ParfaitDataSourceTest {
	private ParfaitDataSource dataSource;

	@Before
	public void setUp() throws SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:parfait", "sa", "");
		DataSource wrapped = new SingleConnectionDataSource(c, false);
		dataSource = new ParfaitDataSource(wrapped);
	}

	@Test
	public void testExecutionCountForCurrentThread() throws SQLException {
		Statement s = dataSource.getConnection().createStatement();
		ThreadMetric counter = dataSource.getCounterMetric();
		assertEquals(0, counter.getValueForThread(Thread.currentThread()));
		s.execute("ROLLBACK");
        assertEquals(1, counter.getValueForThread(Thread.currentThread()));
	}

	@Test
	public void testExecutingNonProxiedMethod() throws SQLException {
		DatabaseMetaData data = dataSource.getConnection().getMetaData();
        assertEquals("HSQL Database Engine", data.getDatabaseProductName());
	}
}
