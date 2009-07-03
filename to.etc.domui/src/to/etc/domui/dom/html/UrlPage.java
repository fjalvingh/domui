package to.etc.domui.dom.html;

import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * The base for all pages that can be accessed thru URL's. This is mostly a
 * dummy class which ensures that all pages/fragments properly extend from DIV,
 * ensuring that the Page logic can replace the "div" tag with a "body" tag for
 * root fragments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2008
 */
public class UrlPage extends Div {
	/** Cached bundle for the page. If the bundle is not found this contains null.. */
	private BundleRef		m_pageBundle;

	/**
	 * Gets called when a page is reloaded (for ROOT pages only).
	 */
	public void		onReload() throws Exception {
	}

	/**
	 * Called when the page gets destroyed (navigation or such).
	 * @throws Exception
	 */
	public void		onDestroy() throws Exception {
	}

	/**
	 * Returns the bundle defined for the page. This defaults to a bundle with the
	 * same name as the page's class name, but can be overridden by an @UIMenu
	 * annotation on the root class.
	 * @return
	 */
	public BundleRef	getPageBundle() {
		if(m_pageBundle == null) {
			m_pageBundle = DomUtil.findPageBundle(this);
			if(m_pageBundle == null)
				throw new ProgrammerErrorException("The page "+this.getClass()+" does not have a page resource bundle");
		}
		return m_pageBundle;
	}

	/**
	 * Lookup and format a message from the page's bundle.
	 * @param key
	 * @param param
	 * @return
	 */
	public String		$(String key, Object... param) {
		BundleRef	br = getPageBundle();
		return br.formatMessage(key, param);
	}
}
