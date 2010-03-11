package to.etc.webapp.query;

import java.sql.*;

import to.etc.webapp.nls.*;

/**
 * Base class for all generic query exceptions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 11, 2010
 */
public class QDbException extends CodeException {
	static public final BundleRef BUNDLE = BundleRef.create(QDbException.class, "messages");

	public QDbException(BundleRef bundle, String code, Object... parameters) {
		super(bundle, code, parameters);
	}

	public QDbException(Throwable t, BundleRef bundle, String code, Object... parameters) {
		super(t, bundle, code, parameters);
	}

	public QDbException(String code, Object... parameters) {
		super(BUNDLE, code, parameters);
	}

	public QDbException(Throwable t, String code, Object... parameters) {
		super(t, BUNDLE, code, parameters);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Translation code.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Create the proper exception from a SQLException type. If no translation is reasonable this
	 * returns NULL; that usually indicates that the original exception must be rethrown. We know of\
	 * the following constraint classes:
	 * <ul>
	 * 	<li>07 Grammar (dynamic SQL errors)</li>
	 * 	<li>20 Grammar Case not found in case stmt (db2)</li>
	 * 	<li>42 Grammar Syntax error or access violation</li>
	 *
	 * 	<li>02 Data Missing data</li>
	 * 	<li>21 Data Cardinality errors (>1 rows when expecting 1 and the like)</li>
	 * 	<li>22 Data Data format errors (invalid date, truncations etc)</li>
	 *
	 * 	<li>23 Constraint General constraint violations</li>
	 * 	<li>27 </li>
	 * 	<li>44 Constraint With check option constraints</li>
	 * </ul>
	 * @param sx
	 * @return
	 */
	static public QDbException findTranslation(Exception x) {
		if(! (x instanceof SQLException))
			return null;
		SQLException sx = (SQLException)x;
		String state= calcSQLState(sx);
		if(state == null || state.length() < 2)
			return null;
		String cat = state.substring(0, 2);

		if("02".equals(cat))
			throw new QNotFoundException(x);
		if("23505".equals(state) || "23515".equals(cat)) // Duplicate key due to unique constraint or index
			return new QDuplicateKeyException(x);

		if("23".equals(cat) || "27".equals(cat) || "44".equals(cat)) {
			return new QConstraintViolationException(x);
		}
		return null;
	}

	/**
	 * For PostgreSQL the actual SQL exception containing the error is often nested. Find
	 * the first one that has a SQLState.
	 * @param x
	 * @return
	 */
	static private String calcSQLState(SQLException x) {
		while(x != null) {
			String state = x.getSQLState();
			if(state != null && state.length() != 0)
				return state;
			x = x.getNextException();
		}
		return null;
	}
}
