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

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.DateConverter;
import to.etc.domui.converter.DateTimeConverter;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;
import to.etc.util.DateUtil;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Date input component: this is an INPUT component with a button attached; pressing
 * the button shows a calendar which can be used to enter a date. The date input must
 * follow the converter rules for the locale. This version allows only a date.
 * <br/>
 * <p>
 * <b>Acceptable input:</b> <br/>
 * '/', '.' or '-' are accepted as separators; for brevity only '/' formats will be listed below:<ul>
 * <br/>
 * <li> 13/3/2012, 23/02/2012 -> dd/mm/yyyy format; adapted to 13-3-2012 and 23-2-2012 leading 0 may be omitted i.e. 02/03/2012 equals 2/3/2012</li>
 * <li> 13/3/13 -> dd/mm/yy format; adapted to 13-3-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may be omitted</li>
 * <li> 13/3, 23/12 -> dd/mm format, adapted to 13-3-2012 and 23-12-2012, year is considered to be the current year; leading 0 may be omitted</li>
 * <br/>
 * <li> 05022013 -> ddmmyyyy format - adapted to 5-2-2013; leading 0 may NOT be omitted</li>
 * <li> 050213 -> ddmmyy format, adapted to 5-2-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may NOT be omitted</li>
 * <li> 0502 -> ddmmyy format, adapted to 5-2-2012, year is considered to be the current year; leading 0 may NOT be omitted</li></ul></p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class DateInput2 extends Text2<Date> {
	private boolean m_withTime;

	private boolean m_withSeconds;

	private boolean m_hideTodayButton;

	private SmallImgButton m_showCalendarButton;

	private SmallImgButton m_todayButton;

	/**
	 * Default constructor creates a date-only input.
	 */
	public DateInput2() {
		this(false);
	}

	/**
	 * Create a date or dateTime input.
	 * @param withtime
	 */
	public DateInput2(boolean withtime) {
		super(Date.class);
		setCssClass("ui-din2");
		setMaxLength(10);
		setSize(10);
		setConverter(ConverterRegistry.getConverterInstance(DateConverter.class));
		//m_selCalButton = new HoverButton("THEME/btn-datein.png");
		//m_selCalButton.setCssClass("ui-di-sib");		// Allow separate styling of these buttons.
		setWithTime(withtime);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		SmallImgButton sib = addButton(FaIcon.faCalendar, a -> { });
		m_showCalendarButton = sib;
		sib.setClicked(null);
		sib.setOnClickJS("WebUI.showCalendar('" + internalGetInput().getActualID() + "'," + isWithTime() + ")");
		internalGetInput().setSpecialAttribute("onblur", "WebUI.dateInputCheckInput(event);");
		if(! m_hideTodayButton) {
			SmallImgButton todayBtn = addButton(FaIcon.faCalendarCheckO, c -> {
				Date currentDate = new Date();
				if(!m_withTime) {
					currentDate = DateUtil.truncateDate(currentDate);
				} else if(!m_withSeconds) {
					currentDate = DateUtil.truncateSeconds(currentDate);
				}
				//modified flag must be set externaly
				DomUtil.setModifiedFlag(DateInput2.this);
				DateInput2.this.setValue(currentDate);
				IValueChanged<?> ovc = getOnValueChanged();
				if(ovc != null) {
					((IValueChanged<NodeBase>) ovc).onValueChanged(DateInput2.this);
				}
			});
			m_todayButton = todayBtn;
		}
		updateButtonState();
	}

	private void updateButtonState() {
		SmallImgButton btn = m_todayButton;
		if(btn != null) {
			btn.setDisabled(isDisabled());
			btn.setDisplay(isReadOnly() ? DisplayType.NONE : null);
		}

		btn = m_showCalendarButton;
		if(null != btn) {
			btn.setDisabled(isDisabled());
			btn.setDisplay(isReadOnly() ? DisplayType.NONE : null);
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		if(isReadOnly() == readOnly)
			return;
		super.setReadOnly(readOnly);
		updateButtonState();
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(isDisabled() == disabled)
			return;
		super.setDisabled(disabled);
		updateButtonState();
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
			setSpecialAttribute("withtime", "true");
		}
		setMaxLength(len);
		setSize(len + 1);						// jal 2014/06/27 Need one extra or the last digit does not show!!
		if (isWithTime()) {
			setConverter(ConverterRegistry.getConverterInstance(DateTimeConverter.class));
		} else {
			setConverter(ConverterRegistry.getConverterInstance(DateConverter.class));
		}
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
	public static DateInput2 createDateInput(Class< ? > clz, String property, boolean editable) {
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(clz, property);
		Class< ? > aclz = pmm.getActualType();
		if(!Date.class.isAssignableFrom(aclz))
			throw new IllegalStateException("Invalid class type=" + Date.class + " for property " + pmm);
		return DateInput2.createDateInput((PropertyMetaModel<Date>) pmm, editable);
	}

	@Nonnull
	public static DateInput2 createDateInput(PropertyMetaModel<Date> pmm, boolean editable) {
		return createDateInput(pmm, editable, false);
	}

	@Nonnull
	public static DateInput2 createDateInput(PropertyMetaModel<Date> pmm, boolean editable, boolean setDefaultErrorLocation) {
		DateInput2 di = new DateInput2();
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
		for(PropertyMetaValidator mpv : pmm.getValidators())
			di.addValidator(mpv);
		return di;
	}
}
