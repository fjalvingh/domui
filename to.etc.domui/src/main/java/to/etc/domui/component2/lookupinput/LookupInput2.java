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

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.tbl.ListQueryHandler;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


/**
 * Lookup input field component.
 *
 * Additional description on use of stylesheets:
 * LookupInput can have this states:
 * <UL>
 * <LI>selected editable -> ui-lui-selected</LI>
 * <LI>selected readonly||disabled -> ui-lui-selected ui-lui-selected-ro</LI>
 * <LI>unselected editable -> ui-lui-v</LI>
 * <LI>unselected readonly -> ui-lui-v ui-ro</LI>
 * </UL>
 *
 * Thing is that when LookupInput is selected (has value), it is
 * rendered as table inside div.
 * When we have readonly LookupInput, then we want only to override
 * background color (not a border), so for that we have additional class =ui-lui-selected-ro.
 * Unselected readonly LookupInput has only one TD in table, so there we
 * can use simple ui-ro, since there we can set border (it would look
 * ackward if we use style named ui-lui-selected-ro for unselected rendering).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
final public class LookupInput2<T> extends LookupInputBase2<T, T> {
	public LookupInput2(@Nonnull Class<T> lookupClass, @Nullable ClassMetaModel metaModel) {
		super(new SameTypeModelFactory<T>(), lookupClass, lookupClass, metaModel, metaModel);
	}

//	public LookupInput(@Nonnull Class<T> lookupClass, @Nonnull String... resultColumns) {
//		super(lookupClass, lookupClass, resultColumns);
//	}
//
	public LookupInput2(@Nonnull Class<T> lookupClass) {
		super(new SameTypeModelFactory<T>(), lookupClass, lookupClass);
	}

	public LookupInput2(@Nonnull QCriteria<T> rootQuery) {
		super(new SameTypeModelFactory<T>(), rootQuery, DomUtil.nullChecked(rootQuery.getBaseClass()));
	}

	public LookupInput2(@Nonnull Class<T> dataClass, @Nonnull List<T> data) {
		super(new SameTypeModelFactory<>(), dataClass, dataClass);
		setQueryHandler(new ListQueryHandler<>(data));
	}

	@Nonnull
	public Class<T> getLookupClass() {
		return getQueryClass();
	}

	@Nonnull
	public ClassMetaModel getMetaModel() {
		return getQueryMetaModel();
	}
}
