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

import to.etc.domui.component.input.Text;
import to.etc.domui.component.misc.LiteralXhtml;

import java.util.List;

public class NodeVisitorBase implements INodeVisitor {

	@Override
	public void visitDiv(Div n) throws Exception {
		sub(n);
	}

	@Override
	public void visitSpan(Span n) throws Exception {
		sub(n);
	}

	@Override
	public void visitUnderline(Underline n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTD(TD n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTR(TR n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTable(Table n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTextNode(TextNode n) throws Exception {
		sub(n);
	}

	@Override
	public void visitXmlNode(XmlTextNode n) throws Exception {
		sub(n);
	}

	@Override
	public void visitPre(Pre n) throws Exception {
		sub(n);
	}

	@Override public void visitP(Para n) throws Exception {
		sub(n);
	}

	@Override
	public void visitLi(Li n) throws Exception {
		sub(n);
	}

	@Override
	public void visitUl(Ul n) throws Exception {
		sub(n);
	}

	@Override
	public void visitA(ATag a) throws Exception {
		sub(a);
	}

	@Override
	public void visitInput(Input n) throws Exception {
		visitNodeBase(n);
	}

	public void visitText(Text< ? > n) throws Exception {
		visitNodeBase(n);
	}

	@Override
	public void visitCheckbox(Checkbox n) throws Exception {
		visitNodeBase(n);
	}

	@Override
	public void visitRadioButton(RadioButton< ? > n) throws Exception {
		visitNodeBase(n);
	}

	@Override
	public void visitTH(TH n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTHead(THead n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTBody(TBody n) throws Exception {
		sub(n);
	}

	@Override
	public void visitImg(Img n) throws Exception {
		visitNodeBase(n);
	}

	@Override
	public void visitButton(Button n) throws Exception {
		sub(n);
	}

	@Override
	public void visitLabel(Label n) throws Exception {
		sub(n);
	}

	@Override
	public void visitOption(SelectOption n) throws Exception {
		sub(n);
	}

	@Override
	public void visitSelect(Select n) throws Exception {
		sub(n);
	}

	@Override
	public void visitBR(BR n) throws Exception {
		sub(n);
	}

	@Override
	public void visitTextArea(TextArea n) throws Exception {
		sub(n);
	}

	@Override
	public void visitFileInput(FileInput fi) throws Exception {
		sub(fi);
	}

	@Override
	public void visitForm(Form n) throws Exception {
		sub(n);
	}

	@Override
	public void visitH(HTag n) throws Exception {
		sub(n);
	}

	public void visitNodeBase(NodeBase n) throws Exception {}

	public void visitNodeContainer(NodeContainer n) throws Exception {}

	@Override
	@Deprecated
	public void visitLiteralXhtml(LiteralXhtml n) throws Exception {
		visitNodeBase(n);
	}

	private void sub(NodeBase n) throws Exception {
		if(n instanceof NodeContainer)
			visitNodeContainer((NodeContainer) n);
		else
			visitNodeBase(n);
	}

	public void visitChildren(NodeContainer c) throws Exception {
		List<NodeBase> ic = c.internalGetChildren();
		for(int i = 0, len = ic.size(); i < len; i++) {
			ic.get(i).visit(this);
		}
	}

	@Override
	public void visitIFrame(IFrame n) throws Exception {
		sub(n);
	}
}
