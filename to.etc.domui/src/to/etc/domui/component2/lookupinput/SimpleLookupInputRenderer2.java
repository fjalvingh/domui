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
package to.etc.domui.component2.lookupinput;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This renderer represents default renderer that is used for {@link LookupInput} control.
 * It can be additionally customized (before and after custom content) by setting provided {@link ICustomContentFactory} fields.
 * See {@link SimpleLookupInputRenderer2#setBeforeContent} and {@link SimpleLookupInputRenderer2#setAfterContent}.
 * Custom added content would be enveloped into separate row(s).
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 10, 2010
 */
public class SimpleLookupInputRenderer2<T> implements INodeContentRenderer<T> {

	public SimpleLookupInputRenderer2() {}

	@Override
	public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable T object, @Nullable Object parameters) throws Exception {
		if(null == object) {
			String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
			node.setText(txt);
			return;
		}

		ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
		if(cmm != null) {
			//-- Has default meta?
			List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
			if(l.size() == 0)
				l = cmm.getComboDisplayProperties();
			if(l.size() > 0) {
				//-- Expand the thingy: render a single line separated with BRs
				List<ExpandedDisplayProperty< ? >> xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
				xpl = ExpandedDisplayProperty.flatten(xpl);
				int c = 0;
				int mw = 0;
				for(ExpandedDisplayProperty< ? > xp : xpl) {
					String val = xp.getPresentationString(object);
					if(val == null || val.length() == 0)
						continue;

					Span vals = new Span();
					vals.setCssClass("ui-lui2-vals");
					node.add(vals);
					vals.setText(val);
				}
				return;
			}
		}

		//-- Use toString
		String txt = object.toString();
		node.setText(txt);
	}
}
