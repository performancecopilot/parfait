package com.custardsource.parfait.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.custardsource.parfait.timing.ThreadMetric;

public class ParfaitDataSourceTest {
	private DataSource wrapped;

	@Before
	public void setUp() throws SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:parfait", "sa", "");
		wrapped = new SingleConnectionDataSource(c, false);
	}

	@Test
	public void testExecutionCountForCurrentThread() throws SQLException {
		ParfaitDataSource source = new ParfaitDataSource(wrapped);
		Statement s = source.getConnection().createStatement();
		ThreadMetric counter = source.getCounterMetric();
		Assert.assertEquals(0, counter.getValueForThread(Thread.currentThread()));
		s.execute("ROLLBACK");
        Assert.assertEquals(1, counter.getValueForThread(Thread.currentThread()));
	}
}
