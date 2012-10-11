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
package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

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

	/**
	 * Default constructor creates a date-only input.
	 */
	public DateInput() {
		this(false);
	}

	/**
	 * Create a date or dateTime input.
	 * @param withtime
	 */
	public DateInput(boolean withtime) {
		super(Date.class);
		setMaxLength(10);
		setSize(10);
		setConverter(ConverterRegistry.getConverterInstance(DateConverter.class));
		m_selCalButton = new SmallImgButton("THEME/btn-datein.png");
		setWithTime(withtime);
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
						if(!m_withTime) {
							currentDate = DateUtil.truncateDate(currentDate);
						} else if(!m_withSeconds) {
							currentDate = DateUtil.truncateSeconds(currentDate);
						}
						//modified flag must be set externaly
						DomUtil.setModifiedFlag(DateInput.this);
						DateInput.this.setValue(currentDate);
						if(getOnValueChanged() != null) {
							((IValueChanged<NodeBase>) getOnValueChanged()).onValueChanged(DateInput.this);
						}
					}
				});
				m_todayButton.setDisplay(isReadOnly() || isDisabled() ? DisplayType.NONE : null);
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
		super.setDisabled(readOnly);
		updateCalendarButtons(readOnly ? DisplayType.NONE : DisplayType.INLINE);
	}

	@Override
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);
		updateCalendarButtons(disabled ? DisplayType.NONE : DisplayType.INLINE);
	}

	private void updateCalendarButtons(@Nonnull DisplayType displayType) {
		m_selCalButton.setDisplay(displayType);
		if(null != m_todayButton)
			m_todayButton.setDisplay(displayType);
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

	@Nonnull
	public static DateInput createDateInput(Class< ? > clz, String property, boolean editable) {
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(clz, property);
		Class< ? > aclz = pmm.getActualType();
		if(!Date.class.isAssignableFrom(aclz))
			throw new IllegalStateException("Invalid class type=" + Date.class + " for property " + pmm);
		return DateInput.createDateInput((PropertyMetaModel<Date>) pmm, editable);
	}

	@Nonnull
	public static DateInput createDateInput(PropertyMetaModel<Date> pmm, boolean editable) {
		return createDateInput(pmm, editable, false);
	}

	@Nonnull
	public static DateInput createDateInput(PropertyMetaModel<Date> pmm, boolean editable, boolean setDefaultErrorLocation) {
		DateInput di = new DateInput();
		if(pmm.isRequired())
			di.setMandatory(true);
		if(!editable)
			di.setDisabled(true);
		if(pmm.getTemporal() == TemporalPresentationType.DATETIME)
			di.setWithTime(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			di.setTitle(s);
		if(setDefaultErrorLocation) {
			di.setErrorLocation(pmm.getDefaultLabel());
		}
		return di;
	}
}
