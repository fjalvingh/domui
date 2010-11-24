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

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Simple combobox handling [String, Object] pairs where the string is the
 * presented label value and the Object represents the values selected.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2009
 */
public class ComboFixed<T> extends ComboComponentBase<ValueLabelPair<T>, T> {
	static private final INodeContentRenderer<ValueLabelPair<Object>> STATICRENDERER = new INodeContentRenderer<ValueLabelPair<Object>>() {
		@Override
		public void renderNodeContent(NodeBase component, NodeContainer node, ValueLabelPair<Object> object, Object parameters) throws Exception {
			node.setText(object.getLabel());
		}
	};

	public ComboFixed() {
		initRenderer();
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> set, INodeContentRenderer<ValueLabelPair<T>> r) {
		super(set, r);
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> dataSetClass) {
		super(dataSetClass);
		initRenderer();
	}

	public ComboFixed(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
		initRenderer();
	}

	public ComboFixed(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
		initRenderer();
	}

	public ComboFixed(List<ValueLabelPair<T>> in) {
		super(in);
		initRenderer();
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void initRenderer() {
		setContentRenderer((INodeContentRenderer) STATICRENDERER); // Another generics fuckup again: you cannot cast this proper, appearently.
	}
	// 20100502 jal Horrible bug! This prevents setting customized option rendering from working!!
	//	@Override
	//	protected void renderOptionLabel(SelectOption o, ValueLabelPair<T> object) throws Exception {
	//		o.add(object.getLabel());
	//	}
}
