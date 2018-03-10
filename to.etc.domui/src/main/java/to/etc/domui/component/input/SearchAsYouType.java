package to.etc.domui.component.input;

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.util.WrappedException;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-2-18.
 */
final public class SearchAsYouType<T> extends SearchAsYouTypeBase<T> implements IControl<T> {
	@Nullable
	private T m_value;

	private List<T> m_data;

	@Nullable
	private IObjectToStringConverter<T> m_converter;

	@Nullable
	private ICompare<T> m_comparator;

	/** For complex classes, this can contain the property whose value we look for */
	@Nullable
	private String m_searchProperty;

	@Nullable
	private IValueChanged<?> m_onValueChanged;

	@Nullable
	private IObjectToStringConverter<T> m_actualConverter;

	private boolean m_mandatory;

	private boolean m_setValueCalled;

	enum MatchMode {
		/** The string entered must be part of the value */
		CONTAINS,

		CONTAINS_CI,

		/** The value must start with the string entered */
		STARTS,

		STARTS_CI
	}

	private enum State {
		EMPTY,
		SELECTED,
		ERROR
	}

	private enum MatchResult {
		UNMATCHED, EXACT, PARTIAL
	}

	/**
	 * Implement this to handle the matching of the input string to
	 * the data type yourself. While you can implement anything you
	 * want here it should, for the end user, be recognisable why
	 * what is typed matches the result.
	 */
	public interface ICompare<T> {
		boolean matches(T instance, String input) throws Exception;
	}

	@Nonnull private MatchMode m_mode = MatchMode.CONTAINS_CI;

	public SearchAsYouType(@Nonnull Class<T> clz) {
		super("ui-sayt", clz);
	}

	public SearchAsYouType(@Nonnull Class<T> clz, String property) {
		super("ui-sayt", clz);
		m_searchProperty = property;
	}


	@Override public void createContent() throws Exception {
		calculateHandler();
		super.createContent();
		updateValue();
	}

	private void updateValue() {
		T value = m_value;
		Input input = getInput();
		if(null == value) {
			input.setRawValue("");
			setState(State.EMPTY);
		} else {
			setState(State.SELECTED);
			IObjectToStringConverter<T> converter = requireNonNull(m_actualConverter);
			input.setRawValue(converter.convertObjectToString(NlsContext.getLocale(), value));
		}
	}

	private void calculateHandler() {
		IObjectToStringConverter<T> cv;
		if(isSimpleType()) {
			cv = getConverter();
			if(null == cv) {
				cv = ConverterRegistry.findConverter(getActualType());
				if(null == cv) {
					cv = (loc, in) -> in.toString();
				}
			}
		} else {
			cv = getConverter();
			if(null == cv) {
				//-- No converter - we must have a property to look for
				String sp = m_searchProperty;
				if(sp != null) {
					PropertyMetaModel<?> spm = getDataModel().getProperty(sp);
					if(spm.getActualType() != String.class)
						throw new ProgrammerErrorException("The property " + spm + " should be of string type. If you do not have that use setConverter to convert the type " + getDataModel() + " to a string to search in");
					cv = (loc, in) -> {
						try {
							return (String) spm.getValue(in);
						} catch(Exception x) {
							throw WrappedException.wrap(x);			// Java's architects are idiots.
						}
					};
				}
			}
		}

		if(cv == null)
			throw new ProgrammerErrorException("You must specify either a property or a converter to handle search on a complex data class");
		m_actualConverter = cv;
	}

	@Override protected IRenderInto<T> getActualRenderer() throws Exception {
		IObjectToStringConverter<T> cv = requireNonNull(m_actualConverter);
		return new IRenderInto<T>() {
			@Override public void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
				node.add(cv.convertObjectToString(NlsContext.getLocale(), object));
			}
		};
	}

	/**
	 * Called when a selection is made.
	 */
	private void selected(T instance) throws Exception {
		if(MetaManager.areObjectsEqual(instance, m_value))
			return;
		changeSelectionValue(instance);
		updateValue();
		//forceRebuild();
	}

	@Override protected void onRowSelected(T value) throws Exception {
		selected(value);
	}

	/**
	 * Changes the selected value but does not reset the component's input.
	 */
	private void changeSelectionValue(T instance) throws Exception {
		setState(instance == null ? State.EMPTY : State.SELECTED);
		if(MetaManager.areObjectsEqual(instance, m_value))
			return;
		if(null != instance)
			clearMessage();
		m_value = instance;
		IValueChanged<?> listener = getOnValueChanged();
		if(null != listener) {
			((IValueChanged<SearchAsYouType<T>>)listener).onValueChanged(this);
		}
	}

	private void setState(State state) {
		switch(state) {
			default:
				throw new IllegalStateException();
			case EMPTY:
				removeCssClass(cssBase("error"));
				removeCssClass(cssBase("selected"));
				break;

			case ERROR:
				addCssClass(cssBase("error"));
				removeCssClass(cssBase("selected"));
				break;

			case SELECTED:
				removeCssClass(cssBase("error"));
				addCssClass(cssBase("selected"));
				break;
		}
	}

	@Override protected void onEmptyInput(boolean done) throws Exception {
		changeSelectionValue(null);
	}

	private MatchResult match(String value, String input) {
		switch(m_mode) {
			default:
				throw new IllegalStateException(m_mode + ": not implemented");

			case CONTAINS_CI:
				value = value.toLowerCase();
				input = input.toLowerCase();
				/*$PASSES_INTO$*/

			case CONTAINS:
				if(value.equals(input.trim()))
					return MatchResult.EXACT;
				return value.contains(input) ? MatchResult.PARTIAL : MatchResult.UNMATCHED;

			case STARTS_CI:
				value = value.toLowerCase();
				input = input.toLowerCase();
				/*$PASSES_INTO$*/

			case STARTS:
				if(value.equals(input.trim()))
					return MatchResult.EXACT;
				return value.startsWith(input) ? MatchResult.PARTIAL : MatchResult.UNMATCHED;
		}
	}

	/**
	 * Check if the typed value is a (unique) match. If so use it, else return a
	 */
	@Nullable @Override protected List<T> onLookupTyping(String input, boolean done) throws Exception {
		List<T> data = getData();
		if(null == data)
			return Collections.emptyList();					// No results

		IObjectToStringConverter<T> cv = requireNonNull(m_actualConverter);
		List<T> result = new ArrayList<>();
		T exact = null;
		for(T datum : data) {
			String string = cv.convertObjectToString(NlsContext.getLocale(), datum);
			MatchResult mr = match(string, input);
			if(mr != MatchResult.UNMATCHED) {
				result.add(datum);
				if(mr == MatchResult.EXACT)
					exact = datum;
				if(result.size() >= MAX_RESULTS)
					break;
			}
		}

		//-- So, what gives....
		if(null != exact) {
			//-- Exact match: set as valid value for now
			if(done) {
				//-- Exact match AND enter was pressed -> select the value and be done
				selected(exact);
				return null;
			}

			//-- Set as current value and make valid, but still show the dropdown.
			m_setValueCalled = false;
			changeSelectionValue(exact);
			if(m_setValueCalled)
				return null;
			//if(isAddSingleMatch())
			//	return null;
			return result;
		}

		changeSelectionValue(null);				// Make sure no value is selected
		setState(State.ERROR);
		return result;
	}

	@Override @Nullable public IValueChanged<?> getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override public void setOnValueChanged(@Nullable IValueChanged<?> onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	@Override
	final public T getValue() {
		try {
			validateBindValue();
			setMessage(null);
			return m_value;
		} catch(ValidationException vx) {
			setMessage(UIMessage.error(vx));
			throw vx;
		}
	}

	@Override public void setValue(@Nullable T v) {
		m_setValueCalled = true;
		if(MetaManager.areObjectsEqual(v, m_value))
			return;
		m_value = v;
		clearAllExtras();
		updateValue();
		clearMessage();
	}

	final public T getBindValue() {
		validateBindValue();
		return m_value;
	}

	final public void setBindValue(T value) {
		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		setValue(value);
	}

	private void validateBindValue() {
		if(isMandatory() && m_value == null) {
			throw new ValidationException(Msgs.MANDATORY);
		}
	}

	@Override public T getValueSafe() {
		return m_value;
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean ro) {
		m_mandatory = ro;
	}

	public List<T> getData() {
		return m_data;
	}

	public SearchAsYouType<T> setData(List<T> data) {
		m_data = data;						// Calling this assumes that the list changed as we do not want to compare all elements
		forceRebuild();
		return this;
	}

	@Nullable public IObjectToStringConverter<T> getConverter() {
		return m_converter;
	}

	public SearchAsYouType<T> setConverter(@Nullable IObjectToStringConverter<T> converter) {
		m_converter = converter;
		return this;
	}

	@Nonnull public MatchMode getMode() {
		return m_mode;
	}


	/**
	 * Sets the string matching mode. This only has effect if no {@link #setComparator(ICompare)} has been set.
	 */
	public SearchAsYouType<T>  setMode(@Nonnull MatchMode mode) {
		if(m_mode == mode)
			return this;
		m_mode = mode;
		forceRebuild();
		return this;
	}

	@Nullable public ICompare<T> getComparator() {
		return m_comparator;
	}

	public SearchAsYouType<T>  setComparator(@Nullable ICompare<T> comparator) {
		m_comparator = comparator;
		forceRebuild();
		return this;
	}

	@Nullable public String getSearchProperty() {
		return m_searchProperty;
	}

	public SearchAsYouType<T>  setSearchProperty(@Nullable String searchProperty) {
		m_searchProperty = searchProperty;
		forceRebuild();
		return this;
	}
}
