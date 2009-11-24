package to.etc.domui.dom;

import to.etc.domui.server.*;

/**
 * Recognises most IE browsers as crapware which needs special renderers to work
 * around their bugs, sigh.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 24, 2009
 */
public class MsCrapwareRenderFactory implements IHtmlRenderFactory {
	private boolean isWrittenByMsMorons(BrowserVersion v) {
		if(!v.isIE())
			return false;
		if(v.getMajorVersion() >= 8)
			return false; // Treat IE8 as a standard browser until we know more...
		return true; // This is IE5, 6, 7.
	}

	@Override
	public HtmlFullRenderer createFullRenderer(BrowserVersion bv, IBrowserOutput o) {
		if(!isWrittenByMsMorons(bv))
			return null;
		return new MsGarbageHtmlFullRenderer(new MsGarbageHtmlTagRenderer(bv, o), o);
	}

	@Override
	public HtmlTagRenderer createTagRenderer(BrowserVersion bv, IBrowserOutput o) {
		if(!isWrittenByMsMorons(bv))
			return null;
		return new MsGarbageHtmlTagRenderer(bv, o);
	}
}
