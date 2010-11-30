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

public interface QNodeVisitor {
	public void visitCriteria(QCriteria< ? > qc) throws Exception;

	public void	visitSelection(QSelection<?> s) throws Exception;

	public void visitUnaryNode(QUnaryNode n) throws Exception;

	public void visitLiteral(QLiteral n) throws Exception;

	public void visitMulti(QMultiNode n) throws Exception;

	public void visitOrder(QOrder o) throws Exception;

	public void visitBetween(QBetweenNode n) throws Exception;

	public void visitPropertyComparison(QPropertyComparison qPropertyComparison) throws Exception;

	public void visitUnaryProperty(QUnaryProperty n) throws Exception;

	public void visitRestrictionsBase(QCriteriaQueryBase<?> n) throws Exception;

	public void visitOrderList(List<QOrder> orderlist) throws Exception;

	public void visitSelectionItem(QSelectionItem n) throws Exception;

	public void visitSelectionColumn(QSelectionColumn qSelectionColumn) throws Exception;

	public void visitPropertySelection(QPropertySelection qPropertySelection) throws Exception;

	public void visitMultiSelection(QMultiSelection n) throws Exception;

	void visitExistsSubquery(QExistsSubquery< ? > q) throws Exception;

	void visitSelectionSubquery(QSelectionSubquery n) throws Exception;

}
