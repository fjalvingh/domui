package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Date input component: this is an INPUT component with a button attached; pressing
 * the button shows a calendar which can be used to enter a date. The date input must
 * follow the converter rules for the locale. This version allows only a date.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class DateInput extends Text<Date> {
	private SmallImgButton m_selCalButton;

	private SmallImgButton m_todayButton;

	private boolean m_withTime;

	private boolean m_withSeconds;

	private boolean m_hideTodayButton;

	public DateInput() {
		super(Date.class);
		setMaxLength(10);
		setSize(10);
		setConverter(ConverterRegistry.getConverterInstance(DateConverter.class));
		m_selCalButton = new SmallImgButton("THEME/btn-datein.png");
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-di");
		m_selCalButton.setOnClickJS("WebUI.showCalendar('" + getActualID() + "'," + isWithTime() + ")");
		setSpecialAttribute("onblur", "WebUI.dateInputCheckInput(event);");
	}

	@Override
	public void onAddedToPage(Page p) {
		appendAfterMe(m_selCalButton);
		if(!m_hideTodayButton) {
			if(m_todayButton == null) {
				m_todayButton = new SmallImgButton("THEME/btnToday.png", new IClicked<SmallImgButton>() {
					@Override
					public void clicked(SmallImgButton b) throws Exception {
						Date currentDate = new Date();
						//modified flag must be set externaly
						DomUtil.setModifiedFlag(DateInput.this);
						DateInput.this.setValue(currentDate);
						if(getOnValueChanged() != null) {
							((IValueChanged<NodeBase>) getOnValueChanged()).onValueChanged(DateInput.this);
						}
					}
				});
			}
			m_selCalButton.appendAfterMe(m_todayButton);
		}
	}

	@Override
	public void onRemoveFromPage(Page p) {
		m_selCalButton.remove(); // Remove selection button
		if(m_todayButton != null)
			m_todayButton.remove();
	}

	/**
	 * The calendar thingy requires calendar files.
	 * FIXME See the comment in {@link Body#onHeaderContributors(Page)}
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onHeaderContributors(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onHeaderContributors(Page page) {
	/*
	 */
	//		page.addHeaderContributor(HeaderContributor.loadJavascript("js/calendar.js"));
	//		page.addHeaderContributor(HeaderContributor.loadJavascript("js/calendar-setup.js"));
	//		Locale	loc = NlsContext.getLocale();				// FIXME Use the locale to decide on which calendar to use.
	//		page.addHeaderContributor(HeaderContributor.loadJavascript("js/calendarnls.js"));
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		m_selCalButton.setDisplay(readOnly ? DisplayType.NONE : null);
		m_todayButton.setDisplay(readOnly ? DisplayType.NONE : null);
	}

	public boolean isWithTime() {
		return m_withTime;
	}

	public void setWithTime(boolean withTime) {
		if(m_withTime == withTime)
			return;
		m_withTime = withTime;
		int len = 10;
		if(isWithTime()) {
			len += 6;
			if(isWithSeconds())
				len += 3;
		}
		setMaxLength(len);
		setSize(len);
		setConverter(ConverterRegistry.getConverterInstance(isWithTime() ? DateTimeConverter.class : DateConverter.class));
	}

	public boolean isWithSeconds() {
		return m_withSeconds;
	}

	public void setWithSeconds(boolean withSeconds) {
		m_withSeconds = withSeconds;
	}

	public boolean isHideTodayButton() {
		return m_hideTodayButton;
	}

	public void setHideTodayButton(boolean hideTodayButton) {
		m_hideTodayButton = hideTodayButton;
	}
}
