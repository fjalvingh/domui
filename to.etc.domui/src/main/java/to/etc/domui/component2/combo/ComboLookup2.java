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
package to.etc.domui.component2.combo;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Combobox component where the list type is the same as the value type, i.e. it
 * uses some {@code List<T>} and getValue() returns T.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class ComboLookup2<T> extends ComboComponentBase2<T, T> {
	public ComboLookup2() {}

	/**
	 * Use the specified cached list maker to fill the combobox.
	 * @param maker
	 */
	public ComboLookup2(IListMaker<T> maker) {
		super(maker);
	}

	public ComboLookup2(List<T> in) {
		super(in);
	}

	public ComboLookup2(Class< ? extends IComboDataSet<T>> set, IRenderInto<T> r) {
		super(set, r);
	}

	/**
	 * Create a combo which fills it's list with the result of the query passed.
	 * @param query
	 */
	public ComboLookup2(QCriteria<T> query) {
		super(query);
	}

	/**
	 * Create a combo which fills it's list with the result of the query passed.
	 * @param query
	 */
	public ComboLookup2(QCriteria<T> query, IRenderInto<T> cr) {
		this(query);
		setContentRenderer(cr);
	}

	/**
	 * Create a combo which fills it's list with the result of the query. Each value is filled from the values of the properties specified.
	 * @param query
	 * @param properties
	 */
	public ComboLookup2(QCriteria<T> query, String... properties) {
		this(query);
		setContentRenderer(new PropertyNodeContentRenderer<>(properties));
	}

	/**
	 * Create a combo which fills it's list with the specified in list. Each value is filled from the values of the properties specified.
	 * @param in
	 * @param properties
	 */
	public ComboLookup2(List<T> in, String... properties) {
		super(in);
		setContentRenderer(new PropertyNodeContentRenderer<>(properties));
	}

	/**
	 * This implements the identical conversion, i.e. in=out, because this component returns
	 * the list type.
	 * @see to.etc.domui.component.input.ComboComponentBase#listToValue(java.lang.Object)
	 */
	@Override
	protected T listToValue(T in) {
		return in;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility methods.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param pmm
	 * @return
	 */
	static public <T> ComboLookup2<T> createLookup(PropertyMetaModel<T> pmm) throws Exception {
		IRenderInto<T> r = (IRenderInto<T>) MetaManager.createDefaultComboRenderer(pmm, null);

		//-- Decide on the combobox' data source to use depending on metadata.
		ComboLookup2<T> co = null;

		//-- Do we have a DataSet provider
		Class< ? extends IComboDataSet<T>> set = (Class< ? extends IComboDataSet<T>>) pmm.getComboDataSet();
		if(set == null) {
			set = (Class< ? extends IComboDataSet<T>>) pmm.getClassModel().getComboDataSet();
		}
		if(set != null)
			co = new ComboLookup2<T>(set, r);
		else {
			//-- No dataset. Create one from a direct Criteria and any query manipulator.
			ClassMetaModel valueModel = pmm.getValueModel();
			if(null == valueModel)
				throw new IllegalStateException(pmm + ": has no valueModel");
			QCriteria<T> q = (QCriteria<T>) valueModel.createCriteria();
			if(null != q) {
				IQueryManipulator<T> qm = pmm.getQueryManipulator();
				if(null == qm)
					qm = (IQueryManipulator<T>) valueModel.getQueryManipulator();

				if(null != qm) {
					q = qm.adjustQuery(q); // Adjust query if needed
					if(q == null)
						throw new IllegalStateException("The query manipulator " + qm + " returned null");
				}

				//-- Handle sorting if applicable
				List<DisplayPropertyMetaModel> dpl = MetaManager.getComboProperties(pmm);
				MetaManager.applyPropertySort(q, dpl);
				co = new ComboLookup2<T>(q, r);
			}
		}
		if(co == null)
			throw new IllegalStateException("I do not have enough information to create the data set for the combobox from the property meta data=" + pmm);

		if(pmm.isRequired())
			co.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			co.setTitle(s);
		return co;
	}


}
