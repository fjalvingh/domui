package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

/**
 * Date input component: this is an INPUT component with a button attached; pressing
 * the button shows a calendar which can be used to enter a date. The date input must
 * follow the converter rules for the locale. This version allows only a date.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class OldDateInput extends Span implements IInputNode<Date> {
	/** The input field for the thingy */
	private Text<Date> m_input;

	private SmallImgButton m_selCalButton;

	private SmallImgButton m_todayButton;

	private boolean m_withTime;

	private boolean m_withSeconds;

	IValueChanged< ? , ? > m_onValueChanged;

	private boolean m_hideTodayButton;

	public OldDateInput() {
		m_input = new Text<Date>(Date.class);
		m_input.setMaxLength(10);
		m_input.setSize(10);
		m_input.setConverterClass(DateConverter.class);
		m_selCalButton = new SmallImgButton("THEME/btn-datein.png");
		setErrorDelegate(m_input); // Delegate this-node's error handling to it's input field
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-di");
		add(m_input);
		add(m_selCalButton);
		m_selCalButton.setOnClickJS("WebUI.showCalendar('" + m_input.getActualID() + "'," + isWithTime() + ")");
		if(!m_hideTodayButton) {
			m_todayButton = new SmallImgButton("THEME/btnToday.png", new IClicked<SmallImgButton>() {
				public void clicked(SmallImgButton b) throws Exception {
					OldDateInput.this.setValue(new Date());
				}
			});
			add(m_todayButton);
		}
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

	/**
	 * Returns the current value in this control. If this is a date-only
	 * control the resulting date value is guaranteed to have it's time
	 * part be all zeroes.
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	public Date getValue() {
		return m_input.getValue();
	}

	public void setValue(Date dt) {
		m_input.setValue(dt);
	}

	public boolean isMandatory() {
		return m_input.isMandatory();
	}

	public void setMandatory(boolean mandatory) {
		m_input.setMandatory(mandatory);
	}

	public boolean isReadOnly() {
		return m_input.isReadOnly();
	}

	public void setReadOnly(boolean readOnly) {
		m_input.setReadOnly(readOnly);
		m_selCalButton.setDisplay(readOnly ? DisplayType.NONE : null);
	}

	public IValueChanged< ? , ? > getOnValueChanged() {
		return m_onValueChanged;
		//		return m_input.getOnValueChanged();
	}

	@SuppressWarnings("unchecked")
	public void setOnValueChanged(IValueChanged< ? , ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
		if(onValueChanged == null)
			m_input.setOnValueChanged(null);
		else
			m_input.setOnValueChanged(new IValueChanged<Text< ? >, Object>() {
				public void onValueChanged(Text< ? > component, Object value) throws Exception {
					((IValueChanged) m_onValueChanged).onValueChanged(OldDateInput.this, OldDateInput.this.getValue());
				}
			});
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
		m_input.setMaxLength(len);
		m_input.setSize(len);
		m_input.setConverterClass(isWithTime() ? DateTimeConverter.class : DateConverter.class);
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
