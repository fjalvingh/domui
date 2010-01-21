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

	public void visitLiteralXhtml(LiteralXhtml n) throws Exception;

	public void visitH(HTag n) throws Exception;
}
