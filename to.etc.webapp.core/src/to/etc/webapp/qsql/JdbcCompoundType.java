package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

class JdbcCompoundType implements IJdbcType, IJdbcTypeFactory {
	private JdbcClassMeta m_compoundMeta;

	public JdbcCompoundType(JdbcClassMeta compoundcm) {
		m_compoundMeta = compoundcm;
	}

	JdbcCompoundType() {}

	public int accept(JdbcPropertyMeta pm) {
		if(pm.isCompound())
			return 10;
		return -1;
	}

	/**
	 * Create a specific converter for this meta property.
	 * @see to.etc.webapp.qsql.IJdbcTypeFactory#createType(to.etc.webapp.qsql.JdbcPropertyMeta)
	 */
	@Override
	public IJdbcType createType(JdbcPropertyMeta pm) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(pm.getActualClass());
		if(!cm.isCompound())
			throw new IllegalStateException("Property " + pm + " has complex type " + pm.getActualClass() + ", but it is not marked as a compound type with @QJdbcCompound");
		return new JdbcCompoundType(cm);
	}

	@Override
	public int columnCount() {
		return m_compoundMeta.getColumnCount();
	}

	/**
	 * Assign values to the columns as rendered in a parameter set.
	 * @see to.etc.webapp.qsql.IJdbcType#assignParameter(java.sql.PreparedStatement, int, to.etc.webapp.qsql.JdbcPropertyMeta, java.lang.Object)
	 */
	@Override
	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta srcpm, Object inst) throws Exception {
		int rix = index;
		for(JdbcPropertyMeta pm : m_compoundMeta.getPropertyList()) {
			if(pm.isTransient())
				continue;
			IJdbcType type = pm.getTypeConverter();

			//-- Get the value for this property
			Object pvalue = inst == null ? null : pm.getPropertyValue(inst);
			type.assignParameter(ps, rix, pm, pvalue);
			rix += type.columnCount();
		}
	}

	@Override
	public Object convertToInstance(ResultSet rs, int index) throws Exception {
		Object inst = m_compoundMeta.getDataClass().newInstance(); // Create empty instance;

		boolean nonnull = false;
		int rix = index;
		for(JdbcPropertyMeta pm : m_compoundMeta.getPropertyList()) {
			if(pm.isTransient())
				continue;
			IJdbcType type = pm.getTypeConverter();
			Object pvalue;
			try {
				pvalue = type.convertToInstance(rs, rix);
			} catch(JdbcConversionException x) {
				throw x;
			} catch(Exception x) {
				throw JdbcConversionException.create(x, rs, pm, rix);
			}

			if(pvalue != null) {
				nonnull = true;
			} else {
				//-- If this is primitive convert if possible.
				if(pm.getActualClass().isPrimitive()) {
					String s = pm.getNullValue();
					if(s == null)
						s = "0";
					pvalue = RuntimeConversions.convertTo(s, pm.getActualClass());
				}
			}
			if(pvalue != null) {
				pm.getPi().getSetter().invoke(inst, pvalue);
			}
			rix += pm.getColumnNames().length;
		}
		return nonnull ? inst : null;
	}
}
