package to.etc.domui.hibernate.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Dialect.SizeStrategy;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

public class FixedSizeStrategyImpl implements SizeStrategy {
	private final Dialect m_dialect;

	private static final double LOG_BASE2OF10 = Math.log(10) / Math.log(2);

	public FixedSizeStrategyImpl(Dialect dialect) {
		m_dialect = dialect;
	}

	@Override
	public Size resolveSize(
		JdbcType jdbcType,
		JavaType<?> javaType,
		Integer precision,
		Integer scale,
		Long length) {
		final var size = new Size();
		// Set the explicit length to null if we encounter the JPA default of 255
		if(length != null && length == Size.DEFAULT_LENGTH) {
			length = null;
		}

		switch(jdbcType.getDdlTypeCode()) {
			case SqlTypes.ARRAY:
				break;
			case SqlTypes.BIT:
			case SqlTypes.CHAR:
			case SqlTypes.NCHAR:
			case SqlTypes.VARCHAR:
			case SqlTypes.NVARCHAR:
			case SqlTypes.BINARY:
			case SqlTypes.VARBINARY:
			case SqlTypes.CLOB:
			case SqlTypes.BLOB:
				size.setLength(javaType.getDefaultSqlLength(m_dialect, jdbcType));
				break;
			case SqlTypes.LONGVARCHAR:
			case SqlTypes.LONGNVARCHAR:
			case SqlTypes.LONGVARBINARY:
				size.setLength(javaType.getLongSqlLength());
				break;
			case SqlTypes.FLOAT:
			case SqlTypes.DOUBLE:
			case SqlTypes.REAL:
				// this is almost always the thing we use:
				length = null;
				size.setPrecision(javaType.getDefaultSqlPrecision(m_dialect, jdbcType));
				// jal 20260205 This is stupid, because it no longer allows specifying the actual presentation form. What is even more stupid is NOT REPORTING WHAT FIELD THIS IS ON!
				//if(scale != null && scale != 0) {
				//	throw new IllegalArgumentException("scale has no meaning for SQL floating point types");
				//}
				// but if the user explicitly specifies the precision, we need to convert it:
				if(precision != null) {
					// convert from base 10 (as specified in @Column) to base 2 (as specified by SQL)
					// using the magic of high school math: log_2(10^n) = n*log_2(10) = n*ln(10)/ln(2)
					precision = (int) Math.ceil(precision * LOG_BASE2OF10);
				}
				break;
			case SqlTypes.TIME:
			case SqlTypes.TIME_WITH_TIMEZONE:
			case SqlTypes.TIME_UTC:
			case SqlTypes.TIMESTAMP:
			case SqlTypes.TIMESTAMP_WITH_TIMEZONE:
			case SqlTypes.TIMESTAMP_UTC:
				length = null;
				size.setPrecision(javaType.getDefaultSqlPrecision(m_dialect, jdbcType));
				if(scale != null && scale != 0) {
					throw new IllegalArgumentException("scale has no meaning for SQL time or timestamp types");
				}
				break;
			case SqlTypes.NUMERIC:
			case SqlTypes.DECIMAL:
			case SqlTypes.INTERVAL_SECOND:
				size.setPrecision(javaType.getDefaultSqlPrecision(m_dialect, jdbcType));
				size.setScale(javaType.getDefaultSqlScale(m_dialect, jdbcType));
				break;
		}

		if(precision != null) {
			size.setPrecision(precision);
		}
		if(scale != null) {
			size.setScale(scale);
		}
		if(length != null) {
			size.setLength(length);
		}
		return size;
	}
}

