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
package to.etc.domui.component.meta.impl;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

import javax.annotation.*;
import java.util.*;

/**
 * This class can be used as a "proxy class" or "delegate class" to another ClassMetaModel
 * instance. You can then override the methods you need changed only while all others are
 * delegated to the original metamodel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 28, 2010
 */
public class ClassMetaModelWrapper implements ClassMetaModel {
	private ClassMetaModel m_parent;

	@Override
	public String getComboSortProperty() {
		return m_parent.getComboSortProperty();
	}

	@Override
	public IQueryManipulator< ? > getQueryManipulator() {
		return m_parent.getQueryManipulator();
	}

	protected ClassMetaModelWrapper(ClassMetaModel parent) {
		m_parent = parent;
	}

	public ClassMetaModel getWrappedModel() {
		return m_parent;
	}

	/**
	 * WATCH OUT: Should only be used when initializing outside the constructor; should not change after this
	 * has been passed to user code.
	 * @param parent
	 */
	public void setWrappedModel(ClassMetaModel parent) {
		m_parent = parent;
	}

	@Override
	public PropertyMetaModel< ? > findProperty(@Nonnull String name) {
		return m_parent.findProperty(name);
	}

	@Override
	@Nonnull
	public PropertyMetaModel< ? > getProperty(@Nonnull String name) {
		return m_parent.getProperty(name);
	}

	@Override
	public PropertyMetaModel< ? > findSimpleProperty(@Nonnull String name) {
		return m_parent.findSimpleProperty(name);
	}

	@Override
	public @Nonnull Class< ? > getActualClass() {
		return m_parent.getActualClass();
	}

	@Nonnull
	@Override
	public BundleRef getClassBundle() {
		return m_parent.getClassBundle();
	}

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_parent.getComboDataSet();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_parent.getComboDisplayProperties();
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_parent.getComboLabelRenderer();
	}

	@Override
	public Class< ? extends IRenderInto< ? >> getComboNodeRenderer() {
		return m_parent.getComboNodeRenderer();
	}

	@Override
	public String getComponentTypeHint() {
		return m_parent.getComponentTypeHint();
	}

	@Override
	public @Nullable SortableType getDefaultSortDirection() {
		return m_parent.getDefaultSortDirection();
	}

	@Override
	public String getDefaultSortProperty() {
		return m_parent.getDefaultSortProperty();
	}

	@Override
	public String getDomainLabel(Locale loc, Object value) {
		return m_parent.getDomainLabel(loc, value);
	}

	@Override
	public Object[] getDomainValues() {
		return m_parent.getDomainValues();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getKeyWordSearchProperties() {
		return m_parent.getKeyWordSearchProperties();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_parent.getLookupSelectedProperties();
	}

	@Override
	public Class< ? extends IRenderInto< ? >> getLookupSelectedRenderer() {
		return m_parent.getLookupSelectedRenderer();
	}

	@Override
	public PropertyMetaModel< ? > getPrimaryKey() {
		return m_parent.getPrimaryKey();
	}

	@Override
	public @Nonnull List<PropertyMetaModel< ? >> getProperties() {
		return m_parent.getProperties();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getSearchProperties() {
		return m_parent.getSearchProperties();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_parent.getTableDisplayProperties();
	}

	@Override
	public String getTableName() {
		return m_parent.getTableName();
	}

	@Override
	public String getUserEntityName() {
		return m_parent.getUserEntityName();
	}

	@Override
	public String getUserEntityNamePlural() {
		return m_parent.getUserEntityNamePlural();
	}

	@Override
	public boolean isPersistentClass() {
		return m_parent.isPersistentClass();
	}

	@Override
	public @Nonnull QCriteria< ? > createCriteria() throws Exception {
		return m_parent.createCriteria();
	}
}
