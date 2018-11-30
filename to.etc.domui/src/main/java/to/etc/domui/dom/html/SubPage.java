package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContextFactory;

/**
 * Replacement for UrlPage for nodes that are meant to act as "subpages" inside
 * an UrlPage. Pages that extends SubPage cannot be used as UrlPage.
 *
 * SubPages have their own data shared data context which is independent from
 * the root page's context (and indeed from its parent SubPages, where applicable).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-18.
 */
abstract public class SubPage extends Div {
	@Override
	abstract public void createContent() throws Exception;


	/**
	 * Allocate the data context specifically for *this* subpage.
	 */
	@NonNull
	public QDataContextFactory getSharedContextFactory(@NonNull String key) {
		return QContextManager.getDataContextFactory(key, getPage().getContextContainer(key));
	}


}
