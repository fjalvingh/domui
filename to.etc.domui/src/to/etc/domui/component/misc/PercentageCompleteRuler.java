package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

public class PercentageCompleteRuler extends Div {
	private int m_percentage;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pct-rlr");
		setWidth("100px");
		setHeight("1em");

		updateValues();
	}

	public int getPercentage() {
		return m_percentage;
	}

	public void setPercentage(int percentage) {
		if(percentage > 100)
			percentage = 100;
		else if(percentage < 0)
			percentage = 0;
		if(m_percentage != percentage) {
			m_percentage = percentage;
			updateValues();
		}
	}

	private void updateValues() {
		setText(Integer.valueOf(m_percentage) + "%");
	}
}
