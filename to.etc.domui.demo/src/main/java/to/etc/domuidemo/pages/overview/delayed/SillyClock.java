package to.etc.domuidemo.pages.overview.delayed;

import java.text.*;
import java.util.*;

import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

public class SillyClock extends PollingDiv {
	private Span m_time;

	private Img m_image;

	private int m_count;

	@Override
	public void createContent() throws Exception {
		m_time = new Span();
		add(m_time);
		m_time.setFontSize("30px");
		m_time.add("Wait...");
		m_image = new Img();
		add(m_image);
		m_image.setVisibility(VisibilityType.HIDDEN);
		m_image.setSrc("img/dancing.gif");
	}

	@Override
	public void checkForChanges() throws Exception {
		Date dt = new Date();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG);
		m_time.setText(df.format(dt));

		m_image.setVisibility(VisibilityType.VISIBLE);
		m_count++;
		if(m_count > 4)
			m_count = 1;
		m_image.setSrc("img/dancing" + m_count + ".gif"); // Dancing the night away ;-)
	}
}
