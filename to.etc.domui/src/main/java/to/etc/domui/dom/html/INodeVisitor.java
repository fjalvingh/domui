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

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.misc.LiteralXhtml;

/**
 * Thingy for visitin gnodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
@NonNullByDefault
public interface INodeVisitor {
	void visitTextNode(TextNode n) throws Exception;

	void visitTR(TR n) throws Exception;

	void visitTD(TD n) throws Exception;

	void visitTable(Table n) throws Exception;

	void visitSpan(Span n) throws Exception;

	void visitDiv(Div n) throws Exception;

	void visitUl(Ul n) throws Exception;

	void visitLi(Li n) throws Exception;

	void visitA(ATag a) throws Exception;

	void visitInput(Input n) throws Exception;

	void visitTHead(THead n) throws Exception;

	void visitTBody(TBody n) throws Exception;

	void visitTH(TH n) throws Exception;

	void visitPre(Pre n) throws Exception;

	void visitImg(Img n) throws Exception;

	void visitCheckbox(Checkbox n) throws Exception;

	void visitRadioButton(RadioButton< ? > b) throws Exception;

	void visitButton(Button b) throws Exception;

	void visitLabel(Label n) throws Exception;

	void visitSelect(Select n) throws Exception;

	void visitOption(SelectOption n) throws Exception;

	void visitBR(BR n) throws Exception;

	void visitHR(HR n) throws Exception;

	void visitTextArea(TextArea n) throws Exception;

	void visitFileInput(FileInput fi) throws Exception;

	void visitForm(Form n) throws Exception;

	void visitLiteralXhtml(LiteralXhtml n) throws Exception;

	void visitH(HTag n) throws Exception;

	void visitXmlNode(XmlTextNode n) throws Exception;

	void visitUnderline(Underline underline) throws Exception;

	void visitIFrame(IFrame n) throws Exception;

	void visitP(Para n) throws Exception;

	void visitColGroup(ColGroup n) throws Exception;

	void visitCol(Col n) throws Exception;

	void visitVideo(Video n) throws Exception;

	void visitSource(Source source) throws Exception;

	void visitCanvas(Canvas c) throws Exception;
}
