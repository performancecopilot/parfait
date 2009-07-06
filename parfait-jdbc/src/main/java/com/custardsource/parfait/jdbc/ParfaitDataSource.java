package com.custardsource.parfait.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import com.custardsource.parfait.timing.AbstractThreadMetric;
import com.custardsource.parfait.timing.ThreadMetric;
import com.google.common.collect.ImmutableList;

public class ParfaitDataSource implements DataSource {
	private DataSource wrapped;

	public ParfaitDataSource(DataSource wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return proxyConnection(wrapped.getConnection());
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return proxyConnection(wrapped.getConnection(username, password));
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return wrapped.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		wrapped.setLogWriter(out);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return wrapped.getLoginTimeout();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		wrapped.setLoginTimeout(seconds);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return wrapped.isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return wrapped.unwrap(iface);
	}

	private Connection proxyConnection(Connection wrapped) {
		return (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class[] { Connection.class }, new ParfaitConnectionHandler(wrapped));
	}

	private class ParfaitConnectionHandler implements InvocationHandler {
		private final Connection wrapped;

		public ParfaitConnectionHandler(Connection wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				String methodName = method.getName();
				if ("createStatement".equals(methodName) || "prepareStatement".equals(methodName)
						|| "prepareCall".equals(methodName)) {
					return proxyStatement((Statement) method.invoke(wrapped, args));
				}
				return method.invoke(proxy, args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		private Object proxyStatement(Statement statement) {
			return Proxy.newProxyInstance(this.getClass().getClassLoader(), statement.getClass()
					.getInterfaces(), new StatementInvocationHandler(statement));
		}
	}

	private class StatementInvocationHandler implements InvocationHandler {

		private Statement target;

		public StatementInvocationHandler(Statement target) {
			this.target = target;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				String methodName = method.getName();
				if ("execute".equals(methodName) || "executeQuery".equals(methodName)
						|| "executeUpdate".equals(methodName) || "executeBatch".equals(methodName)) {
					return invokeAndLogExecute(method, args);
				}
				return method.invoke(target, args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		private Object invokeAndLogExecute(Method method, Object[] args) throws Exception {
			long start = System.currentTimeMillis();
			try {
				return method.invoke(target, args);
			} finally {
				executionCounts.get().incrementAndGet();
				executionTimes.get().addAndGet(System.currentTimeMillis() - start);
			}
		}
	}

	public final long getExecutionCountForCurrentThread() {
		return executionCounts.get().longValue();
	}

	public final long getExecutionTimeForCurrentThread() {
		return executionTimes.get().longValue();
	}

	private final ThreadLocal<AtomicLong> executionCounts = new ThreadLocal<AtomicLong>() {
		@Override
		protected AtomicLong initialValue() {
			return new AtomicLong(0);
		}
	};
	private final ThreadLocal<AtomicLong> executionTimes = new ThreadLocal<AtomicLong>() {
		@Override
		protected AtomicLong initialValue() {
			return new AtomicLong(0);
		}
	};

	public final ThreadMetric getCounterMetric() {
		return new AbstractThreadMetric("Database call count", "", "db.count",
				"Number of database calls made during event") {

			@Override
			public long getCurrentValue() {
				return getExecutionCountForCurrentThread();
			}
		};
	}

	public final ThreadMetric getTimeMetric() {
		return new AbstractThreadMetric("Database execution time", "", "db.time",
				"Time spent in database calls during event") {
			@Override
			public long getCurrentValue() {
				return getExecutionTimeForCurrentThread();
			}
		};
	}

	public final Collection<ThreadMetric> getThreadMetrics() {
		return ImmutableList.<ThreadMetric> of(getCounterMetric(), getTimeMetric());
	}
}