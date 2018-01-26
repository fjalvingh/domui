/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
		return v.isIE();
	}

	@Override
	public HtmlFullRenderer createFullRenderer(BrowserVersion bv, IBrowserOutput o, boolean uiTestMode) {
		if(!isWrittenByMsMorons(bv))
			return null;
		return new MsGarbageHtmlFullRenderer(new MsGarbageHtmlTagRenderer(bv, o, uiTestMode), o);
	}

	@Override
	public HtmlTagRenderer createTagRenderer(BrowserVersion bv, IBrowserOutput o, boolean uiTestMode) {
		if(!isWrittenByMsMorons(bv))
			return null;
		return new MsGarbageHtmlTagRenderer(bv, o, uiTestMode);
	}
}
