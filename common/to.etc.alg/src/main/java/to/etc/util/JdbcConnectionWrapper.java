package to.etc.util;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class JdbcConnectionWrapper implements Connection {
	private final Connection m_connection;

	public JdbcConnectionWrapper(Connection connection) {
		m_connection = connection;
	}
	@Override
	public Statement createStatement() throws SQLException {
		return m_connection.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return m_connection.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return m_connection.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return m_connection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		m_connection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return m_connection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		m_connection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		m_connection.rollback();
	}

	@Override
	public void close() throws SQLException {
		m_connection.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return m_connection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return m_connection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		m_connection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return m_connection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		m_connection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return m_connection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		m_connection.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return m_connection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return m_connection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		m_connection.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return m_connection.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return m_connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return m_connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return m_connection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		m_connection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		m_connection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return m_connection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return m_connection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return m_connection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		m_connection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		m_connection.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return m_connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return m_connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return m_connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return m_connection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return m_connection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return m_connection.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return m_connection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return m_connection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return m_connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return m_connection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return m_connection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		m_connection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		m_connection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return m_connection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return m_connection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return m_connection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return m_connection.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		m_connection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return m_connection.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		m_connection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		m_connection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return m_connection.getNetworkTimeout();
	}

	@Override
	public void beginRequest() throws SQLException {
		m_connection.beginRequest();
	}

	@Override
	public void endRequest() throws SQLException {
		m_connection.endRequest();
	}

	@Override
	public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
		return m_connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
	}

	@Override
	public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
		return m_connection.setShardingKeyIfValid(shardingKey, timeout);
	}

	@Override
	public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
		m_connection.setShardingKey(shardingKey, superShardingKey);
	}

	@Override
	public void setShardingKey(ShardingKey shardingKey) throws SQLException {
		m_connection.setShardingKey(shardingKey);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return m_connection.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return m_connection.isWrapperFor(iface);
	}
}
