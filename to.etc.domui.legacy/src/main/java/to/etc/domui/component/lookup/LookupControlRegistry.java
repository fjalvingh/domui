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
package to.etc.domui.component.lookup;

import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Default Registry of Lookup control factories.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 23, 2008
 */
@Deprecated
final public class LookupControlRegistry {
	static public final LookupControlRegistry INSTANCE = new LookupControlRegistry();

	@Nonnull
	private List<ILookupControlFactory> m_factoryList = new ArrayList<ILookupControlFactory>();

	public LookupControlRegistry() {
		register(new LookupFactoryString());
		register(new LookupFactoryDate());
		register(new LookupFactoryNumber());
		register(new LookupFactoryNumber2());
		register(new LookupFactoryRelation());
		register(new LookupFactoryEnumAndBool());
		register(new LookupFactoryRelationCombo());
	}

	@Nonnull
	public synchronized List<ILookupControlFactory> getFactoryList() {
		return m_factoryList;
	}

	public synchronized void register(@Nonnull ILookupControlFactory f) {
		m_factoryList = new ArrayList<ILookupControlFactory>(m_factoryList);
		m_factoryList.add(f);
	}

	@Nullable
	public ILookupControlFactory findFactory(@Nonnull SearchPropertyMetaModel pmm) {
		ILookupControlFactory best = null;
		int score = 0;
		for(ILookupControlFactory cf : getFactoryList()) {
			int v = cf.accepts(pmm, null);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	@Nonnull
	public ILookupControlFactory getControlFactory(@Nonnull SearchPropertyMetaModel pmm) {
		ILookupControlFactory cf = findFactory(pmm);
		if(cf == null)
			throw new IllegalStateException("Cannot get a Lookup Control factory for " + pmm);
		return cf;
	}

	@Nonnull
	public <T, X extends NodeBase & IControl<T>> ILookupControlFactory getLookupQueryFactory(@Nonnull final SearchPropertyMetaModel pmm, @Nonnull X control) {
		ILookupControlFactory best = null;
		int score = 0;
		for(ILookupControlFactory cf : getFactoryList()) {
			int v = cf.accepts(pmm, control);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		if(best == null)
			throw new IllegalStateException("Cannot get a Lookup Control QueryFragment factory for " + pmm + " and control " + control);
		return best;
	}
}
