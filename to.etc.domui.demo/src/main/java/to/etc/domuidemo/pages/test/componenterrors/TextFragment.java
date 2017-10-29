package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class TextFragment extends Div {
	private String		m_t20;

	private String		m_t21 = "zzaabb";

	private Integer	m_t22 = Integer.valueOf("12345");

	private String		m_t23;

	private BigDecimal m_t30;

	private BigDecimal m_t31 = new BigDecimal("123.45");

	@Override public void createContent() throws Exception {
		add(new Caption("Text component"));

		TBody tb = addTable("With form builder", "Without form builder");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new TextF4Fragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new TextRawFragment());
	}
}
