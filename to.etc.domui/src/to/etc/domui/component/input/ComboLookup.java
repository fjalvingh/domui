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
package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Combobox component where the list type is the same as the value type, i.e. it
 * uses some {@code List<T>} and getValue() returns T.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class ComboLookup<T> extends ComboComponentBase<T, T> {
	public ComboLookup() {}

	public ComboLookup(IListMaker<T> maker) {
		super(maker);
	}

	public ComboLookup(List<T> in) {
		super(in);
	}

	public ComboLookup(Class< ? extends IComboDataSet<T>> set, INodeContentRenderer<T> r) {
		super(set, r);
	}

	public ComboLookup(QCriteria<T> query) {
		super(query);
	}

	@Override
	protected T listToValue(T in) throws Exception {
		return in;
	}
}
