package to.etc.webapp.qsql;

import to.etc.webapp.query.*;

/**
 * Used for computing value of calculated fields.
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 22 Oct 2009
 */
public interface IInitializable {
	void initializeInstance(QDataContext dc) throws Exception;
}
