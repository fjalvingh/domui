package to.etc.domui.util;

import to.etc.domui.component.misc.ALink;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.util.WrappedException;
import to.etc.webapp.mailer.ITextLinkRenderer;
import to.etc.webapp.mailer.TextLinkInfo;
import to.etc.webapp.query.IIdentifyable;

import javax.annotation.Nonnull;

/**
 * Helps with rendering a log message as DomUI linked text. Uses TextLinkInfo as factory
 * to find DomUI pages belonging to links.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 29, 2010
 */
final public class DomUILinkedTextRenderer implements ITextLinkRenderer {
	private NodeContainer m_c;

	public DomUILinkedTextRenderer() {}

	public void setContainer(@Nonnull NodeContainer c) {
		m_c = c;
	}

	@Override
	public void appendLink(@Nonnull String rurl, @Nonnull String text) {
		//-- Is this a DomUI url?
		String page, query;
		int pos = rurl.indexOf('?');
		if(pos == -1) {
			page = rurl;
			query = null;
		} else {
			page = rurl.substring(0, pos);
			query = rurl.substring(pos + 1);
		}

		ALink link;
		String ext = DomApplication.get().getUrlExtension();

		if(!page.endsWith("." + ext)) {
			//-- Non-DomUI link.
			rurl = UIContext.getRequestContext().getRelativePath(rurl);
			link = new ALink(rurl, null, null);
		} else {
			//-- DomUI page. Extract the class
			page = page.substring(0, page.length() - ext.length() - 1);
			Class< ? > clz;
			try {
				clz = m_c.getClass().getClassLoader().loadClass(page);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
			if(!(UrlPage.class.isAssignableFrom(clz)))
				throw new IllegalStateException("Class " + clz + " is not a DomUI class");
			Class< ? extends UrlPage> pageClz = (Class< ? extends UrlPage>) clz;


			PageParameters pp = new PageParameters();
			if(null != query) {
				pp = PageParameters.decodeParameters(query);
			}
			link = new ALink(pageClz, pp);
		}

		m_c.add(link);
		link.add(text);
	}

	@Override
	public void appendText(@Nonnull String text) {
		int pos = 0;
		int len = text.length();
		while(pos < len) {
			int nl = text.indexOf("\n", pos);
			if(nl == -1) {
				DomUtil.renderHtmlString(m_c, text.substring(pos));
				return;
			}
			if(nl > pos) {
				DomUtil.renderHtmlString(m_c, text.substring(pos, nl));
			}
			pos = nl + 1;
			m_c.add(new BR());
		}
	}

	static public void register(@Nonnull Class< ? extends IIdentifyable< ? >> dataClass, @Nonnull Class< ? extends UrlPage> page, String paramName) {
		TextLinkInfo.register(dataClass, DomUtil.createPageRURL(page, null) + "?" + paramName + "={id}");
	}

	static public void register(@Nonnull String linkName, @Nonnull Class< ? extends UrlPage> page, @Nonnull String paramName) {
		TextLinkInfo.register(linkName, DomUtil.createPageRURL(page, null) + "?" + paramName + "={id}");
	}

}
