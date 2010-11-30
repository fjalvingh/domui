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
package to.etc.domui.dom.html;

import to.etc.domui.component.misc.*;

/**
 * Thingy for visitin gnodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public interface INodeVisitor {
	public void visitTextNode(TextNode n) throws Exception;

	public void visitTR(TR n) throws Exception;

	public void visitTD(TD n) throws Exception;

	public void visitTable(Table n) throws Exception;

	public void visitSpan(Span n) throws Exception;

	public void visitDiv(Div n) throws Exception;

	public void visitUl(Ul n) throws Exception;

	public void visitLi(Li n) throws Exception;

	public void visitA(ATag a) throws Exception;

	public void visitInput(Input n) throws Exception;

	public void visitTHead(THead n) throws Exception;

	public void visitTBody(TBody n) throws Exception;

	public void visitTH(TH n) throws Exception;

	public void visitImg(Img n) throws Exception;

	public void visitCheckbox(Checkbox n) throws Exception;

	public void visitRadioButton(RadioButton b) throws Exception;

	public void visitButton(Button b) throws Exception;

	public void visitLabel(Label n) throws Exception;

	public void visitSelect(Select n) throws Exception;

	public void visitOption(SelectOption n) throws Exception;

	public void visitBR(BR n) throws Exception;

	public void visitTextArea(TextArea n) throws Exception;

	public void visitFileInput(FileInput fi) throws Exception;

	public void visitForm(Form n) throws Exception;

	@Deprecated
	public void visitLiteralXhtml(LiteralXhtml n) throws Exception;

	public void visitH(HTag n) throws Exception;

	public void visitXmlNode(XmlTextNode n) throws Exception;
}
