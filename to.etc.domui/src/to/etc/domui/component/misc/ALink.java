package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * A hyperlink which allows for opening windows in a separate conversation from the
 * link. This is a rather complex interaction which works as follows:
 * <ul>
 *	<li>The link is generated as an A tag with both an onclick handler and a href URL.</li>
 *	<li>The href url contains a server-generated link to the Page to reach. This URL does NOT contain a WID.</li>
 *	<li>The onclick handler is a regular handler passing control to the server <b>and returns false always</b></li>
 *	<li>When the link is clicked in a normal way the onclick handles takes precedence over the href. This onclick
 *		handler passes control to the server as usual; the server will send a redirect for the actual page to reach
 *		and this redirect contains the current WID. This causes the new page to show in the current window session.</li>
 *	<li>If the link is opened with the right mouse button and "Open in new window" or something like that then
 *		the onclick handler is not used; the HREF url is used instead. This causes the browser to open a new window
 *		with a new URL not containing a WID. The server will respond by redirecting to the same thing with a new WID.
 *		This establishes a new Window session.</li>
 * </ul>
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2008
 */
public class ALink extends ATag {
	/** The target class this link should move to. When set targetURL must be null. */
	private Class< ? extends UrlPage> m_targetClass;

	/** The target URL this page should move to. When set targetClass must be null. */
	private String m_targetURL;

	private PageParameters m_targetParameters;

	private WindowParameters m_newWindowParameters;

	private MoveMode m_moveMode = MoveMode.SUB;

	private String m_imageUrl;

	public ALink() {}

	/**
	 * Link to a new page; the new page is a SUB page (it is added to the shelve stack).
	 * @param targetClass
	 */
	public ALink(Class< ? extends UrlPage> targetClass) {
		m_targetClass = targetClass;
		updateLink();
	}

	public ALink(Class< ? extends UrlPage> targetClass, MoveMode mode) {
		m_targetClass = targetClass;
		m_moveMode = mode;
		updateLink();
	}

	/**
	 * Link to a new page; the new page is a SUB page (it is added to the shelve stack).
	 * @param targetClass
	 * @param targetParameters
	 */
	public ALink(Class< ? extends UrlPage> targetClass, PageParameters targetParameters) {
		m_targetClass = targetClass;
		m_targetParameters = targetParameters;
		updateLink();
	}

	public ALink(Class< ? extends UrlPage> targetClass, PageParameters targetParameters, MoveMode mode) {
		m_targetClass = targetClass;
		m_targetParameters = targetParameters;
		m_moveMode = mode;
		updateLink();
	}

	public ALink(Class< ? extends UrlPage> targetClass, PageParameters targetParameters, WindowParameters newWindowParameters) {
		m_targetClass = targetClass;
		m_targetParameters = targetParameters;
		m_newWindowParameters = newWindowParameters;
	}

	/**
	 * Link to some http: url that is not a DomUI page.
	 *
	 * @param targetURL
	 * @param targetParameters
	 */
	public ALink(String targetURL, PageParameters targetParameters, WindowParameters newWindowParameters) {
		m_targetURL = targetURL;
		m_targetParameters = targetParameters;
		m_newWindowParameters = newWindowParameters;
		updateLink();
	}

	public Class< ? extends UrlPage> getTargetClass() {
		return m_targetClass;
	}

	public void setTargetClass(Class< ? extends UrlPage> targetClass, Object... parameters) {
//		if(m_targetClass == targetClass)
//			return;
		m_targetClass = targetClass;
		if(parameters == null || parameters.length == 0)
			m_targetParameters = null;
		else
			m_targetParameters = new PageParameters(parameters);
		updateLink();
	}

	public PageParameters getTargetParameters() {
		return m_targetParameters;
	}

	public void setTargetParameters(PageParameters targetParameters) {
		if(DomUtil.isEqual(m_targetParameters, targetParameters))
			return;
		m_targetParameters = targetParameters;
		updateLink();
	}

	public WindowParameters getNewWindowParameters() {
		return m_newWindowParameters;
	}

	public void setNewWindowParameters(WindowParameters newWindowParameters) {
		if(DomUtil.isEqual(m_newWindowParameters, newWindowParameters))
			return;
		m_newWindowParameters = newWindowParameters;
		updateLink();
	}

	public MoveMode getMoveMode() {
		return m_moveMode;
	}

	public void setMoveMode(MoveMode moveMode) {
		m_moveMode = moveMode;
	}

	/**
	 * Generate the actual link to the thing.
	 */
	private void updateLink() {
		if(m_targetClass != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(PageContext.getRequestContext().getRelativePath(m_targetClass.getName()));
			sb.append(".ui");
			DomUtil.addUrlParameters(sb, m_targetParameters, true);
			setHref(sb.toString()); // Append URL only, without WID
		} else if(! DomUtil.isBlank(m_targetURL)) {
			setHref(m_targetURL);
		} else {
			setHref(null);
			return;
		}

		if(getClicked() == null && getNewWindowParameters() != null) {
			//-- Generate an onclick javascript thingy to open the window to prevent popup blockers.
			//-- We need a NEW window session. Create it,
			RequestContextImpl ctx = (RequestContextImpl) PageContext.getRequestContext();

			//-- Send a special JAVASCRIPT open command, containing the shtuff.
			StringBuilder sb = new StringBuilder();
			String wid = DomUtil.generateGUID();
			sb.append("return DomUI.openWindow('");

			if(!DomUtil.isBlank(m_targetURL)) {
				sb.append(DomUtil.createPageURL(m_targetURL, m_targetParameters));
			} else {
				sb.append(ctx.getRelativePath(m_targetClass.getName()));
				sb.append('.');
				sb.append(DomApplication.get().getUrlExtension());
				sb.append('?');
				StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
				sb.append('=');
				sb.append(wid);
				sb.append(".x");
				if(m_targetParameters != null) {
					DomUtil.addUrlParameters(sb, m_targetParameters, false);
				}
			}
			sb.append("','");
			sb.append(wid);
			sb.append("','");

			sb.append("resizable=");
			sb.append(m_newWindowParameters.isResizable() ? "yes" : "no");
			sb.append(",scrollbars=");
			sb.append(m_newWindowParameters.isShowScrollbars() ? "yes" : "no");
			sb.append(",toolbar=");
			sb.append(m_newWindowParameters.isShowToolbar() ? "yes" : "no");
			sb.append(",location=");
			sb.append(m_newWindowParameters.isShowLocation() ? "yes" : "no");
			sb.append(",directories=");
			sb.append(m_newWindowParameters.isShowDirectories() ? "yes" : "no");
			sb.append(",status=");
			sb.append(m_newWindowParameters.isShowStatus() ? "yes" : "no");
			sb.append(",menubar=");
			sb.append(m_newWindowParameters.isShowMenubar() ? "yes" : "no");
			sb.append(",copyhistory=");
			sb.append(m_newWindowParameters.isCopyhistory() ? "yes" : "no");

			if(m_newWindowParameters.getWidth() > 0) {
				sb.append(",width=");
				sb.append(m_newWindowParameters.getWidth());
			}
			if(m_newWindowParameters.getHeight() > 0) {
				sb.append(",height=");
				sb.append(m_newWindowParameters.getHeight());
			}
			sb.append("');");
			setOnClickJS(sb.toString());
		}
	}

	@Override
	public boolean internalNeedClickHandler() {
		return getClicked() != null || getNewWindowParameters() == null;
	}

	/**
	 * Overridden click handler. If no specific onClick handler is configured we handle the click by
	 * moving to the specified page within the same window session.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#internalOnClicked()
	 */
	@Override
	public void internalOnClicked() throws Exception {
		if(getClicked() != null) {
			super.internalOnClicked();
			return;
		}

		//-- Default action.
		if(m_targetClass == null)
			return;

		//-- Is this a WINDOWED link?
		if(m_newWindowParameters != null) {
			//-- We need a NEW window session. Create it,
			RequestContextImpl ctx = (RequestContextImpl) PageContext.getRequestContext();
			WindowSession cm = ctx.getSession().createWindowSession();

			//-- Send a special JAVASCRIPT open command, containing the shtuff.
			StringBuilder sb = new StringBuilder();
			sb.append("DomUI.openWindow('");
			sb.append(ctx.getRelativePath(m_targetClass.getName()));
			sb.append(".ui?");
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(cm.getWindowID());
			sb.append(".x");
			if(m_targetParameters != null)
				DomUtil.addUrlParameters(sb, m_targetParameters, false);
			sb.append("','");
			sb.append(cm.getWindowID());
			sb.append("','");

			sb.append("resizable=");
			sb.append(m_newWindowParameters.isResizable() ? "yes" : "no");
			sb.append(",scrollbars=");
			sb.append(m_newWindowParameters.isShowScrollbars() ? "yes" : "no");
			sb.append(",toolbar=");
			sb.append(m_newWindowParameters.isShowToolbar() ? "yes" : "no");
			sb.append(",location=");
			sb.append(m_newWindowParameters.isShowLocation() ? "yes" : "no");
			sb.append(",directories=");
			sb.append(m_newWindowParameters.isShowDirectories() ? "yes" : "no");
			sb.append(",status=");
			sb.append(m_newWindowParameters.isShowStatus() ? "yes" : "no");
			sb.append(",menubar=");
			sb.append(m_newWindowParameters.isShowMenubar() ? "yes" : "no");
			sb.append(",copyhistory=");
			sb.append(m_newWindowParameters.isCopyhistory() ? "yes" : "no");

			if(m_newWindowParameters.getWidth() > 0) {
				sb.append(",width=");
				sb.append(m_newWindowParameters.getWidth());
			}
			if(m_newWindowParameters.getHeight() > 0) {
				sb.append(",height=");
				sb.append(m_newWindowParameters.getHeight());
			}
			sb.append("');");
			appendJavascript(sb.toString());
			return;
		}

		//-- Normal link; moveTo.
		PageContext.getRequestContext().getWindowSession().internalSetNextPage(m_moveMode, m_targetClass, null, null, m_targetParameters);
	}

	public void setImage(final String url) {
		if(DomUtil.isEqual(url, m_imageUrl))
			return;
		m_imageUrl = url;
		if(m_imageUrl != null) {
			addCssClass("ui-alnk");
		} else {
			removeCssClass("ui-alnk");
		}
		changed();
		updateStyle();
		forceRebuild();
	}

	public String getImage() {
		return m_imageUrl;
	}

	private void updateStyle() {
		setBackgroundImage(PageContext.getRequestContext().translateResourceName(m_imageUrl));
	}

}
