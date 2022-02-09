package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.DropDownPicker.HAlign;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.webapp.nls.NlsContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Encapsulates AutocompleteText and drop down picker into single component. Input behaves as autocomplete field that does search on select inside select within drop down picker.
 * Search is done at client side for faster user experience.
 * It also allows entering of new data (not contained inside predefined list), that is why it works on-top of String based input.
 */
public class EditableDropDownPicker<T> extends AutocompleteText {
	@NonNull
	private final List<T> m_data;

	@NonNull
	private final DropDownPicker<T> m_picker;

	@Nullable
	private IIconRef m_dropDownIcon;

	@NonNull
	private HAlign m_halign = DropDownPicker.HAlign.LEFT;

	@Nullable
	private IObjectToStringConverter<T> m_toStringConverter;

	@Nullable
	private T m_object;

	@NonNull
	private final Class<T> m_type;

	/**
	 * Empty constructor.
	 * Before use, make sure to setup component using:
	 * <UL>
	 * <LI> {@link EditableDropDownPicker#setDropDownIcon(IIconRef)} </LI>
	 * <LI> {@link EditableDropDownPicker#setToStringConverter(IObjectToStringConverter)} in case of 'type' is not assignable from String.class</LI>
	 * </UL>
	 */
	public EditableDropDownPicker(@NonNull Class<T> type, @NonNull List<T> data) {
		super();
		m_type = type;
		m_data = data;
		m_picker = new DropDownPicker<>(m_data);
	}

	/**
	 * Factory constructor.
	 *
	 * @param toStringConverter In case of T = String, toStringConverter can be left null, otherwise it needs to be specified.
	 */
	public EditableDropDownPicker(@NonNull Class<T> type, @NonNull List<T> data, @NonNull IIconRef dropDownIcon, @Nullable IObjectToStringConverter<T> toStringConverter) {
		this(type, data);
		m_dropDownIcon = dropDownIcon;
		m_toStringConverter = toStringConverter;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();

		if(m_dropDownIcon != null && !isReadOnly()) {
			m_picker.setSrc(m_dropDownIcon);
		}

		m_picker.setMandatory(isMandatory());
		m_picker.setValue(null);
		m_picker.setDisabled(isDisabled());
		m_picker.setHalign(m_halign);
		m_picker.setAlignmentBase(this);
		m_picker.setOnBeforeShow((sender, combo) -> {
			String text = getValueSafe();
			clearMessage();
			adjustSelection(combo, text);
		});

		m_picker.setOnValueChanged((IValueChanged<DropDownPicker<T>>) component -> {
			T value = m_object = component.getValueSafe();
			setValue(convertObjectToString(NlsContext.getCurrencyLocale(), value));
			setFocus();
			//appendJavascript("$('#" + getActualID() + "').focus();");
			IValueChanged<EditableDropDownPicker<T>> onValueChanged = (IValueChanged<EditableDropDownPicker<T>>) getOnValueChanged();
			if(onValueChanged != null) {
				onValueChanged.onValueChanged(EditableDropDownPicker.this);
			}
		});
		m_picker.setFocusOnBlur(this);

		setSelect(m_picker.getSelectControl());
		initSelectSizeAndValue();
		appendAfterMe(m_picker);
		m_picker.build();
		initializeJS();
	}

	protected String convertObjectToString(Locale currencyLocale, T val) {
		if(m_toStringConverter != null) {
			return m_toStringConverter.convertObjectToString(NlsContext.getCurrencyLocale(), val);
		} else {
			return ConverterRegistry.getConverter(m_type, null).convertObjectToString(NlsContext.getCurrencyLocale(), val);
		}
	}

	private void adjustSelection(ComboLookup<T> combo, String text) throws Exception {
		boolean found = false;
		DropDownPicker<T> picker = Objects.requireNonNull(m_picker);
		for(int i = 0; i < combo.getData().size(); i++) {
			T val = combo.getData().get(i);
			String optionText = convertObjectToString(NlsContext.getCurrencyLocale(), val);
			if(text != null && text.equals(optionText)) {
				combo.setValue(val);
				picker.setButtonValue(text);
				found = true;
				break;
			}
		}
		initSelectSizeAndValue();
		if(!found) {
			picker.setButtonValue(null);
		}
	}

	/**
	 * Gets picker select options.
	 */
	public @NonNull List<T> getData() {
		return m_data;
	}

	private void initSelectSizeAndValue() throws Exception {
		int size = m_data.size();
		if(isMandatory()) {
			setComboSize(size == 1 ? 2 : size);
			//workaround: we have to set a value to avoid rendering of empty option for mandatory combo
			if(!m_data.isEmpty()) {
				T firstVal = m_data.get(0);
				if(null != firstVal) {
					((ComboLookup<T>) m_picker.getSelectControl()).setValue(firstVal);
				}
			}
		} else {
			setComboSize(size + 1);
		}
	}

	private void setComboSize(int size) throws Exception {
		int newSize = size > DropDownPicker.DEFAULT_COMBO_SIZE ? DropDownPicker.DEFAULT_COMBO_SIZE : size; 
		m_picker.setSize(newSize);
	}

	@Nullable
	public IIconRef getDropDownIcon() {
		return m_dropDownIcon;
	}

	public void setDropDownIcon(@Nullable IIconRef dropDownIcon) {
		m_dropDownIcon = dropDownIcon;
		if(m_picker != null) {
			m_picker.setSrc(dropDownIcon);
		}
	}

	@NonNull
	public HAlign getHalign() {
		return m_halign;
	}

	public void setHalign(@NonNull HAlign halign) {
		m_halign = halign;
		if(m_picker != null) {
			m_picker.setHalign(halign);
		}
	}

	@Nullable
	public IObjectToStringConverter<T> getToStringConverter() {
		return m_toStringConverter;
	}

	public void setToStringConverter(@Nullable IObjectToStringConverter<T> toStringConverter) {
		m_toStringConverter = toStringConverter;
	}

	@Nullable
	public T getObject() {
		return m_object;
	}

	public void setObject(@Nullable T value) {
		m_object = value;
	}
}
