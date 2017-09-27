package to.etc.dbutil.schema;

import to.etc.dbutil.reverse.Reverser;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class DbColumn implements Serializable {
	private final Boolean m_autoIncrement;

	private DbTable m_table;

	private String m_name;

	private ColumnType m_type;

	private int m_precision;

	private int m_scale;

	private boolean m_nullable;

	private String m_comment;

	private int m_sqlType;

	private String m_platformTypeName;

	public DbColumn(DbTable table, String name, ColumnType type, int precision, int scale, boolean nullable, Boolean autoIncrement) {
		m_table = table;
		m_name = name;
		m_type = type;
		m_precision = precision;
		m_scale = scale;
		m_nullable = nullable;
		m_autoIncrement = autoIncrement;
	}

	@Nonnull
	public Reverser r() {
		return m_table.r();
	}

	public int getPrecision() {
		return m_precision;
	}

	public void setPrecision(int precision) {
		m_precision = precision;
	}

	public int getScale() {
		return m_scale;
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

	public ColumnType getType() {
		return m_type;
	}

	public void setType(ColumnType type) {
		m_type = type;
	}

	public String getName() {
		return m_name;
	}

	public DbTable getTable() {
		return m_table;
	}

	public String getComment() {
		return m_comment;
	}

	public void setComment(String comment) {
		m_comment = comment;
	}

	public boolean isNullable() {
		return m_nullable;
	}

	public void setNullable(boolean nullable) {
		m_nullable = nullable;
	}

	public String getPlatformTypeName() {
		return m_platformTypeName;
	}

	public void setPlatformTypeName(String platformTypeName) {
		m_platformTypeName = platformTypeName;
	}

	public int getSqlType() {
		return m_sqlType;
	}

	public void setSqlType(int sqlType) {
		m_sqlType = sqlType;
	}

	public void setValue(PreparedStatement ps, int ix, Object object) throws Exception {
		if(null == object) {
			ps.setNull(ix, getSqlType());
			return;
		}

		switch(getType().getSqlType()){
			default:
				ps.setString(ix, (String) object);
				break;

			case Types.DATE:
			case Types.TIMESTAMP:
				ps.setTimestamp(ix, (Timestamp) object);
				break;

			case Types.BOOLEAN:
			case Types.BIT:
				ps.setBoolean(ix, (Boolean) object);
				break;

			case Types.BIGINT:
				ps.setLong(ix, ((BigDecimal) object).longValue());		// IMPORTANT
				break;

			case Types.INTEGER:
				ps.setInt(ix, ((BigDecimal) object).intValue());		// IMPORTANT
				break;

			case Types.NUMERIC:
			case Types.DECIMAL:
				ps.setBigDecimal(ix, (BigDecimal) object);
				break;

			case Types.DOUBLE:
			case Types.FLOAT:
				ps.setDouble(ix, (Double) object);
				break;

			case Types.BLOB:
				final byte[] data = (byte[]) object;
				Blob b = new Blob() {

					@Override
					public long length() throws SQLException {
						return data.length;
					}

					@Override
					public byte[] getBytes(long pos, int length) throws SQLException {
						byte[] res = new byte[length];
						System.arraycopy(data, (int) pos, res, 0, length);
						return res;
					}

					@Override
					public InputStream getBinaryStream() throws SQLException {
						return new ByteArrayInputStream(data);
					}

					@Override
					public long position(byte[] pattern, long start) throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public long position(Blob pattern, long start) throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public int setBytes(long pos, byte[] bytes) throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public OutputStream setBinaryStream(long pos) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void truncate(long len) throws SQLException {
						// TODO Auto-generated method stub

					}

					@Override
					public void free() throws SQLException {
						// TODO Auto-generated method stub

					}

					@Override
					public InputStream getBinaryStream(long pos, long length) throws SQLException {
						return new ByteArrayInputStream(data);
					}


				};
				ps.setBlob(ix, b);

				//				ByteArrayInputStream baos = new ByteArrayInputStream(data);
				//				ps.setBinaryStream(ix, baos, data.length);
				break;


			case Types.BINARY:
				ps.setBytes(ix, (byte[]) object);
				break;
		}
	}

	public Object getValue(ResultSet rs, int ix) throws Exception {
		Object val;
		switch(getType().getSqlType()){
			default:
				val = rs.getString(ix);
				break;

			case Types.DATE:
			case Types.TIMESTAMP:
				val = rs.getTimestamp(ix);
				break;

			case Types.BOOLEAN:
			case Types.BIT:
				val = Boolean.valueOf(rs.getBoolean(ix));
				break;

			case Types.NUMERIC:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.DECIMAL:
				val = rs.getBigDecimal(ix);
				break;

			case Types.DOUBLE:
			case Types.FLOAT:
				val = Double.valueOf(rs.getDouble(ix));
				break;
		}
		if(rs.wasNull())
			val = null;
		return val;
	}

	public boolean isLob() {
		switch(getType().getSqlType()){
			default:
				return false;

			case Types.BLOB:
			case Types.BINARY:
				return true;

		}
	}

	public String getTypeString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getPlatformTypeName());

		if(r().typeHasPrecision(this)) {
			if(getPrecision() > 0) {
				sb.append('(');
				sb.append(getPrecision());
				if(r().typeHasScale(this)) {

					if(getScale() >= 0) {
						sb.append(',').append(getScale());
					}
				}
				sb.append(')');
			}
		}
		if(isNullable()) {
			sb.append(" null");
		} else
			sb.append(" not null");
		return sb.toString();
	}

	public Boolean isAutoIncrement() {
		return m_autoIncrement;
	}

	@Override
	public String toString() {
		return m_table.getSchema().getName() + "." + m_table.getName() + "." + getName();
	}

}
