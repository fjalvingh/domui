package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.DropDownPicker.HAlign;
import to.etc.domui.component.input.DropDownPicker.IDropDownPickerAdjuster;
import to.etc.domui.component.layout.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.webapp.nls.*;

/**
 * Encapsulates input and drop down picker into single component. Input behaves as autocomplete field that does search on select inside select within drop down picker.
 * Search is done at client side for faster user experience. 
 * It also allows entering of new data (not contained inside predefined list), that is why it works on-top of String based input.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 6, 2012
 */
public class EditableDropDownPicker<T> extends ConnectedToSelectInput implements IObjectToStringConverter<T> {
	private DropDownPicker<T> m_picker;

	private final List<T> m_data;

	private final String m_dropDownIcon;

	private final HAlign m_halign = DropDownPicker.HAlign.LEFT;

	private final IObjectToStringConverter<T> m_toStringConverter;

	public EditableDropDownPicker(@Nonnull List<T> data, @Nonnull String dropDownIcon, @Nullable IObjectToStringConverter<T> toStringConverter) {
		super();
		m_data = data;
		m_dropDownIcon = dropDownIcon;
		m_toStringConverter = toStringConverter;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		NodeBase parentWindow = getParentOfTypes(Window.class, UrlPage.class);
		NodeBase zIndexNode = parentWindow != null ? parentWindow : this; //we use this to calculate correct zIndex in drop down picker later

		m_picker = new DropDownPicker<T>(zIndexNode, m_data);
		if(m_dropDownIcon != null) {
			m_picker.setSrc(m_dropDownIcon);
		}

		m_picker.setMandatory(isMandatory());
		m_picker.setSelectedValue(null);
		m_picker.setHalign(m_halign);
		m_picker.setAlignmentBase(this);
		m_picker.setAdjuster(new IDropDownPickerAdjuster<T>() {
			@Override
			public void onBeforeShow(ComboLookup<T> combo) throws Exception {
				String text = getValueSafe();
				clearMessage();
				adjustSelection(combo, text);
			}
		});

		m_picker.setOnValueSelected(new IValueSelected<T>() {

			@Override
			public void valueSelected(@Nullable T value) throws Exception {
				setValue(convertObjectToString(NlsContext.getCurrencyLocale(), value));
				IValueChanged<EditableDropDownPicker<T>> onValueChanged = (IValueChanged<EditableDropDownPicker<T>>) EditableDropDownPicker.this.getOnValueChanged();
				if(onValueChanged != null) {
					onValueChanged.onValueChanged(EditableDropDownPicker.this);
				}
			}
		});

		setConnectedControl(m_picker);
		appendAfterMe(m_picker);
		m_picker.build();
	}

	@Override
	protected void afterCreateContent() throws Exception {
		super.afterCreateContent();
		initialize();
	}

	protected void adjustSelection(ComboLookup<T> combo, String text) throws Exception {
		boolean found = false;
		for(int i = 0; i < combo.getData().size(); i++) {
			T val = combo.getData().get(i);
			String optionText = convertObjectToString(NlsContext.getCurrencyLocale(), val);
			if(text != null && text.equals(optionText)) {
				combo.setValue(val);
				m_picker.setValue(text);
				found = true;
				break;
			}
		}
		if(combo.getData().size() < combo.getSize()) {
			combo.setSize(found ? combo.getData().size() : combo.getData().size() + 1);
		}
		if(!found) {
			m_picker.setValue(null);
		}
	}

	@Override
	public String convertObjectToString(Locale loc, T in) throws UIException {
		if(m_toStringConverter == null) {
			return in.toString();
		} else {
			return m_toStringConverter.convertObjectToString(loc, in);
		}
	}
}
