package to.etc.domui.util.db;

import to.etc.webapp.query.*;

/**
 * EXPERIMENTAL INTERFACE
 * Pluggable interface to copy a source model to a target model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 8, 2010
 */
public class QCopy {
	static private IModelCopier m_copier;

	static public void setImplementation(IModelCopier m) {
		m_copier = m;
	}

	static private IModelCopier c() {
		return m_copier;
	}

	static public <T> T copyInstanceShallow(QDataContext dc, T source) throws Exception {
		return c().copyInstanceShallow(dc, source);
	}

	static public <T> T copyDeep(QDataContext dc, T source) throws Exception {
		return c().copyInstanceDeep(dc, source);
	}


}
