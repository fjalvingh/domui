package to.etc.domui.hibernate.config;

import org.hibernate.exception.ConstraintViolationException;
import to.etc.domui.component.misc.ExceptionDialog.ExceptionPresentation;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.IBundleCode;

import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-11-2023.
 */
public class HibernateMessageDecoder {
	static private final Map<String, IBundleCode> m_constraintMessageMap = new ConcurrentHashMap<>();

	/**
	 * This tries to convert hibernate exceptions into something more edible.
	 */
	static public ExceptionPresentation translateHibernateException(Throwable x) {
		//-- Ordered!!
		if(x instanceof ConstraintViolationException) {
			ConstraintViolationException cvx = (ConstraintViolationException) x;
			String sqlState = cvx.getSQLState();
			if(null == sqlState)
				return null;

			//-- First: try to find a message by constraint name
			String constraintName = cvx.getConstraintName();
			IBundleCode code = m_constraintMessageMap.get(constraintName);
			if(null != code)
				return new ExceptionPresentation(code.format());

			//-- Generic errors
			if(sqlState.equals("23505")) {				// Unique constraint violation?
				return new ExceptionPresentation(Msgs.sqlErrNotUnique.format());
			}
		}

		if(x instanceof PersistenceException) {
			PersistenceException px = (PersistenceException) x;
			return translateHibernateException(px.getCause());
		}

		return null;
	}

	static public void registerConstraintMessage(String constraintName, IBundleCode code) {
		m_constraintMessageMap.put(constraintName, code);
	}
}
