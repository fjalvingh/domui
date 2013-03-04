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
package to.etc.webapp.query;

import java.util.*;

import javax.annotation.*;

public interface QNodeVisitor {
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception;

	public void visitSelection(@Nonnull QSelection< ? > s) throws Exception;

	public void visitUnaryNode(@Nonnull QUnaryNode n) throws Exception;

	public void visitLiteral(@Nonnull QLiteral n) throws Exception;

	public void visitMulti(@Nonnull QMultiNode n) throws Exception;

	public void visitOrder(@Nonnull QOrder o) throws Exception;

	public void visitBetween(@Nonnull QBetweenNode n) throws Exception;

	public void visitPropertyComparison(@Nonnull QPropertyComparison qPropertyComparison) throws Exception;

	public void visitUnaryProperty(@Nonnull QUnaryProperty n) throws Exception;

	public void visitRestrictionsBase(@Nonnull QCriteriaQueryBase< ? > n) throws Exception;

	public void visitOrderList(List<QOrder> orderlist) throws Exception;

	public void visitSelectionItem(@Nonnull QSelectionItem n) throws Exception;

	public void visitSelectionColumn(@Nonnull QSelectionColumn qSelectionColumn) throws Exception;

	public void visitPropertySelection(@Nonnull QPropertySelection qPropertySelection) throws Exception;

	public void visitMultiSelection(@Nonnull QMultiSelection n) throws Exception;

	void visitExistsSubquery(@Nonnull QExistsSubquery< ? > q) throws Exception;

	void visitSelectionSubquery(@Nonnull QSelectionSubquery n) throws Exception;

}
