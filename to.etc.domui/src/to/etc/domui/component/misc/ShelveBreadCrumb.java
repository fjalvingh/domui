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
package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

/**
 * Shows the current shelved path has a breadcrumb, and allows the user to move up into that path.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 27, 2008
 */
public class ShelveBreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		WindowSession cm = UIContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		List<ShelvedEntry> stack = cm.getShelvedPageStack();
		if(stack.size() == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}
		setDisplay(null);

		for(int i = 0; i < stack.size(); i++) {
			Page p = stack.get(i).getPage();

			if(i > 0) {
				//-- Append the marker,
				Span s = new Span();
				add(s);
				s.add(new TextNode(" > "));
			}

			//-- Create a LINK or a SPAN
			Span s = new Span();
			add(s);
			String ttl = p.getBody().getTitle();
			if(ttl == null || ttl.length() == 0) {
				ttl = p.getBody().getClass().getName();
				ttl = ttl.substring(ttl.lastIndexOf('.') + 1);
			}

			s.setText(ttl);
		}
	}
}
