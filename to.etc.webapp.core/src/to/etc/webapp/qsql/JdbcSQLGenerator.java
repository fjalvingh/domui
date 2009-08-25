package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

/**
 * Generate a SQL query from a QCriteria selection using the poor man's JDBC code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcSQLGenerator extends QNodeVisitorBase {
	private StringBuilder m_fields = new StringBuilder();

	private int m_nextFieldIndex = 1;

	private Map<String, PClassRef> m_tblMap = new HashMap<String, PClassRef>();

	private PClassRef m_root;

	/** The list of all retrievers for a single row */
	private List<IInstanceMaker> m_retrieverList = new ArrayList<IInstanceMaker>();

	@Override
	public void visitCriteria(QCriteria< ? > qc) throws Exception {
		m_root = new PClassRef(qc.getBaseClass(), "this_");
		m_tblMap.put(m_root.getAlias(), m_root);
		generateClassGetter(m_root);
	}

	private void generateClassGetter(PClassRef root) {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(root.getDataClass()); // Will throw exception if not proper jdbc class.
		int startIndex = m_nextFieldIndex;
		for(JdbcPropertyMeta pm : cm.getPropertyList()) {
			if(m_fields.length() != 0)
				m_fields.append(",");
			m_fields.append(root.getAlias());
			m_fields.append(".");
			m_fields.append(pm.getColumnName());
			m_nextFieldIndex++;
		}
		m_retrieverList.add(new ClassInstanceMaker(root, startIndex, cm));
	}

	public String getSQL() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ");
		sb.append(m_fields);
		sb.append(" from ");

		JdbcClassMeta cm = JdbcMetaManager.getMeta(m_root.getDataClass());
		sb.append(cm.getTableName());
		sb.append(" ");
		sb.append(m_root.getAlias());

		return sb.toString();
	}


	public JdbcQuery getQuery() {
		return new JdbcQuery(getSQL(), m_retrieverList);


	}
}
