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
package to.etc.domui.component.tree;

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.IHasChangeListener;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.TD;
import to.etc.domui.util.JavascriptUtil;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * A tree component that allows selection of multiple items.
 * @param <T>
 */
@DefaultNonNull
public class TreeSelectMulti<T> extends Tree<T> implements IHasChangeListener {
	@Nonnull
	private Set<T> m_value = new HashSet<>();

	@Nullable
	private IValueChanged< ? > m_onValueChanged;

	public TreeSelectMulti() {}

	public TreeSelectMulti(ITreeModel<T> model) {
		super(model);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		appendCreateJS(JavascriptUtil.disableSelection(this));
	}

	@Nonnull
	public Set<T> getValue() {
		return new HashSet<>(m_value);
	}

	public void setValue(@Nullable Set<T> value) throws Exception {
		internalSetValue(value, false);
	}

	private void internalSetValue(@Nullable Set<T> value, boolean callListeners) throws Exception {
		if(null == value)
			value = nullChecked(Collections.EMPTY_SET);				// Workaround compiler bug
		if(MetaManager.areObjectsEqual(value, m_value))
			return;
		Set<T> newSet = new HashSet<>(value);						// Dup because source can be immutable

		//-- Remove everything not allowed by the predicate
		INodePredicate<T> predicate = getNodeSelectablePredicate();
		if(null != predicate) {
			for(T item : value) {
				if(! predicate.predicate(item))
					newSet.remove(item);
			}
		}

		//-- Now unselect anything no longer selected
		Set<T> currentSet = new HashSet<>(m_value);
		currentSet.removeAll(newSet);								// Remove anything that IS selected, leaving those that were selected before but no more
		for(T oldItem : currentSet) {
			markAsSelected(oldItem, false);
		}

		//-- And select everything not selected before
		Set<T> addedSet = new HashSet<>(newSet);
		addedSet.removeAll(m_value);								// Remove all current selections, leaving those that need to be selected
		for(T newItem: addedSet) {
			markAsSelected(newItem, true);
		}
		m_value = newSet;
		if(callListeners)
			internalOnValueChanged();
	}

	@Override
	public boolean isSelectable(@Nullable T node) throws Exception {
		INodePredicate<T> predicate = getNodeSelectablePredicate();
		if(predicate == null)
			return true;
		return predicate.predicate(node);
	}

	@Override
	protected void cellClicked(TD cell, T value, ClickInfo clinfo) throws Exception {
		boolean append = clinfo.isControl() || clinfo.isShift();
		Set<T> set = new HashSet<>(m_value);
		boolean select = ! set.contains(value);

		if(! append) {
			set.clear();
		}
		if(select)
			set.add(value);
		else
			set.remove(value);

		internalSetValue(set, true);
		super.cellClicked(cell, value, clinfo);
	}

	@Override
	protected boolean isSelected(@Nullable T node) {
		return m_value.contains(node);
	}

	@Nullable
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(@Nullable IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}
}
