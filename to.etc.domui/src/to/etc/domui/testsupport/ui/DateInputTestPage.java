package to.etc.domui.testsupport.ui;

import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Selenium test date input check page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 27, 2014
 */
public class DateInputTestPage extends UrlPage {
	private boolean m_withtime;

	@Nullable
	private Div m_result;

	@UIUrlParameter(name = "withtime")
	public boolean isWithtime() {
		return m_withtime;
	}

	public void setWithtime(boolean withtime) {
		m_withtime = withtime;
	}

	@Override
	public void createContent() throws Exception {
		final DateInput di = new DateInput(isWithtime());
		preventAlertsFromOpening();
		add(di);
		di.setTestID("datein");

		DefaultButton	button = new DefaultButton("Click", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				Date dt = di.getValueSafe();
				Div result = m_result;
				if(null == result) {
					m_result = result = new Div();
					add(result);
					result.setTestID("result");
				}
				result.removeAllChildren();
				if(dt == null) {
					result.setText("error");
				} else {
					DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
					result.setText(df.format(dt));
				}
			}
		});
		button.setTestID("btn");
		add(button);

		DefaultButton clear = new DefaultButton("Click", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				di.setValue(null);
				Div result = m_result;
				if(null != result) {
					result.remove();
					m_result = null;
				}
			}
		});
		clear.setTestID("clear");
		add(clear);
	}

	/**
	 * Prevent alert from opening since it's presence will be indicated in result div as an error.
	 * @return
	 */
	protected void preventAlertsFromOpening() {
		StringBuilder sb = new StringBuilder();
		sb.append("var defaultAlert = alert;");
		sb.append("alert = function(message){");
		sb.append("		console.log('Alert blocked: ' + message);");
		sb.append("}");
		this.appendCreateJS(DomUtil.nullChecked(sb.toString()));
	}

}
