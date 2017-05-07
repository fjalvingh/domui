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
package to.etc.domui.dom.html;

import to.etc.webapp.nls.*;

/**
 * A THEAD node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
public class THead extends NodeContainer {
	public THead() {
		super("thead");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTHead(this);
	}

	public TH[] setHeaders(String... labels) {
		forceRebuild();
		TR row = new TR();
		add(row);
		TH[] res = new TH[labels.length];
		int ix = 0;
		for(String s : labels) {
			TH th = new TH();
			row.add(th);
			th.setText(s);
			res[ix++] = th;
		}
		return res;
	}

	public TH[] setHeaders(BundleRef b, String... keys) {
		forceRebuild();
		TR row = new TR();
		add(row);
		TH[] res = new TH[keys.length];
		int ix = 0;
		for(String s : keys) {
			TH th = new TH();
			row.add(th);
			th.setText(b.getString(s));
			res[ix++] = th;
		}
		return res;
	}

	public void setHeaderCssClasses(String cls) {
		for(TH th : getChildren(TH.class))
			th.setCssClass(cls);
	}

	private TR getRow() {
		for(int i = getChildCount(); --i >= 0;) {
			NodeBase b = getChild(i);
			if(b instanceof TR)
				return (TR) b;
		}
		TR tr = new TR();
		add(tr);
		return tr;
	}

	public TH addHeader(String text) {
		TR tr = getRow();
		TH th = new TH();
		tr.add(th);
		th.setText(text);
		return th;
	}
	public TH addHeader(NodeBase node) {
		TR tr = getRow();
		TH th = new TH();
		tr.add(th);
		th.add(node);
		return th;
	}
}
