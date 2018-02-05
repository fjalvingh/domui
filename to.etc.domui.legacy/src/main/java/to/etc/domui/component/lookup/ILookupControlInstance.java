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

import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encapsulates a single created lookup "part" in the lookup form, and
 * defines the possible actions that we can define on it. This should
 * be able to return it's presentation, and it should be able to add
 * it's restrictions (caused by the user entering data in it's controls)
 * to a QCriteria.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
@Deprecated
public interface ILookupControlInstance<T> {

	/**
	 * Represents result of {@link ILookupControlInstance#appendCriteria(QCriteria)}.
	 * Can be one of values:
	 * <OL>
	 * <LI>{@link AppendCriteriaResult#INVALID}</LI>
	 * <LI>{@link AppendCriteriaResult#EMPTY}</LI>
	 * <LI>{@link AppendCriteriaResult#VALID}</LI>
	 * </OL>
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 6 Jan 2011
	 */
	enum AppendCriteriaResult {
		/** Entered search critaria is not valid. */
		INVALID, //
		/** No search critaria is entered. This however can result in implicit filter added into criteria. */
		EMPTY, //
		/** Search critaria is entered by user, represents explicit filters.
		 * This information is important when we want to force user to define at least one explicit criteria in order to permit search on large data sets. */
		VALID
	}

	/**
	 * Return all of the nodes (input and otherwise) that together form the complete visual representation
	 * of this lookup line. This may NOT return null OR an empty list.
	 */
	NodeBase[] getInputControls();

	/**
	 * Returns the control where the label should be attached to. Can return null, in that case the first
	 * IInput control or the first node in the list will be used.
	 */
	NodeBase getLabelControl();

	/**
	 * Sets the input(s) to disabled state.
	 * @param disabled
	 */
	void setDisabled(boolean disabled);

	/**
	 * When called this should clear all data input into the control instances, causing them to
	 * be empty (not adding to the restrictions set).
	 */
	void clearInput();

	/**
	 * Evaluate the contents of the input for this lookup line; if the user has
	 * added data there then add the values to the query.
	 */
	AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception;

	/**
	 * Return the value entered into this control.
	 * This may return null.
	 * @return
	 */
	@Nullable
	T getValue();

	/**
	 * Sets the value in the control
	 * @param value
	 */
	void setValue(@Nullable T value) throws Exception;
}
