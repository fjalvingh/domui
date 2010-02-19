package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;
import to.etc.domui.component.misc.*;

public class NodeVisitorBase implements INodeVisitor {

	public void visitDiv(Div n) throws Exception {
		sub(n);
	}

	public void visitSpan(Span n) throws Exception {
		sub(n);
	}

	public void visitTD(TD n) throws Exception {
		sub(n);
	}

	public void visitTR(TR n) throws Exception {
		sub(n);
	}

	public void visitTable(Table n) throws Exception {
		sub(n);
	}

	public void visitTextNode(TextNode n) throws Exception {
		sub(n);
	}

	@Override
	public void visitXmlNode(XmlTextNode n) throws Exception {
		sub(n);
	}

	public void visitLi(Li n) throws Exception {
		sub(n);
	}

	public void visitUl(Ul n) throws Exception {
		sub(n);
	}

	public void visitA(ATag a) throws Exception {
		sub(a);
	}

	public void visitInput(Input n) throws Exception {
		visitNodeBase(n);
	}

	public void visitText(Text< ? > n) throws Exception {
		visitNodeBase(n);
	}

	public void visitCheckbox(Checkbox n) throws Exception {
		visitNodeBase(n);
	}

	public void visitRadioButton(RadioButton n) throws Exception {
		visitNodeBase(n);
	}

	public void visitTH(TH n) throws Exception {
		sub(n);
	}

	public void visitTHead(THead n) throws Exception {
		sub(n);
	}

	public void visitTBody(TBody n) throws Exception {
		sub(n);
	}

	public void visitImg(Img n) throws Exception {
		visitNodeBase(n);
	}

	public void visitButton(Button n) throws Exception {
		sub(n);
	}

	public void visitLabel(Label n) throws Exception {
		sub(n);
	}

	public void visitOption(SelectOption n) throws Exception {
		sub(n);
	}

	public void visitSelect(Select n) throws Exception {
		sub(n);
	}

	public void visitBR(BR n) throws Exception {
		sub(n);
	}

	public void visitTextArea(TextArea n) throws Exception {
		sub(n);
	}

	public void visitFileInput(FileInput fi) throws Exception {
		sub(fi);
	}

	public void visitForm(Form n) throws Exception {
		sub(n);
	}

	public void visitH(HTag n) throws Exception {
		sub(n);
	}

	public void visitNodeBase(NodeBase n) throws Exception {}

	public void visitNodeContainer(NodeContainer n) throws Exception {}

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
		for(NodeBase b : c)
			b.visit(this);
	}
}
