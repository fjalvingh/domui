package to.etc.domui.component.input;

import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Input;
import to.etc.domui.util.IRenderInto;
import to.etc.util.WrappedException;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

	enum MatchMode {
		/** The string entered must be part of the value */
		CONTAINS,

		CONTAINS_CI,

		/** The value must start with the string entered */
		STARTS,

		STARTS_CI
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

	@Override public void createContent() throws Exception {
		calculateHandler();
		super.createContent();

		updateValue();
	}

	private void updateValue() {
		T value = m_value;
		Input input = getInput();
		if(null == value) {
			input.setRawValue(null);
			setState(State.EMPTY);
		} else {
			setState(State.SELECTED);
			IObjectToStringConverter<T> converter = Objects.requireNonNull(m_actualConverter);
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
		setHandler(new ComplexHandler(cv));
		m_actualConverter = cv;

		//-- By default render a string using the converter.
		KeyWordPopupRowRenderer<T> rr = new KeyWordPopupRowRenderer<>(getDataModel());
		IObjectToStringConverter<T> finalCv = cv;				// Really, their stupidity seems without bounds.
		rr.addColumns("", (IRenderInto<T>) (node, object) -> node.add(finalCv.convertObjectToString(NlsContext.getLocale(), object)));
		setRowRenderer(rr);
	}

	/**
	 * Called when a selection is made.
	 */
	private void selected(T instance) throws Exception {
		if(MetaManager.areObjectsEqual(instance, m_value))
			return;
		m_value = instance;
		forceRebuild();
		IValueChanged<?> listener = getOnValueChanged();
		if(null != listener) {
			((IValueChanged<SearchAsYouType<T>>)listener).onValueChanged(this);
		}
	}

	private void enterPressed(String value) throws Exception {
		List<T> res = getHandler().queryFromString(value, 10);
		if(res.size() == 1) {
			selected(res.get(0));
			return;
		}
		setState(State.ERROR);
	}

	enum State {
		EMPTY,
		SELECTED,
		ERROR
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

	@Override protected void internalOnTyping() {
		setState(State.EMPTY);
	}

	private boolean match(String value, String input) {
		switch(m_mode) {
			default:
				throw new IllegalStateException(m_mode + ": not implemented");
			case CONTAINS:
				return value.contains(input);

			case CONTAINS_CI:
				return value.toLowerCase().contains(input.toLowerCase());

			case STARTS:
				return value.startsWith(input);

			case STARTS_CI:
				return value.toLowerCase().startsWith(input.toLowerCase());
		}
	}

	@NotNull private List<T> findMatchesUsingConverter(@Nonnull String input, int max, IObjectToStringConverter<T> cv) {
		List<T> data = getData();
		if(null == data)
			return Collections.emptyList();
		List<T> result = new ArrayList<>();
		for(T datum : data) {
			String string = cv.convertObjectToString(NlsContext.getLocale(), datum);
			if(match(string, input)) {
				result.add(datum);
				if(result.size() >= max)
					break;
			}
		}
		return result;
	}

	@Override @Nullable public IValueChanged<?> getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override public void setOnValueChanged(@Nullable IValueChanged<?> onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	@Override public void setValue(@Nullable T v) {

	}

	@Nullable @Override public T getValue() {
		return null;
	}

	@Override public T getValueSafe() {
		return null;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean ro) {
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

	/**
	 * The type is some kind of class from which we use a single property or a converter to search on.
	 */
	private final class ComplexHandler implements IQuery<T> {
		private final IObjectToStringConverter<T> m_fieldConverter;

		public ComplexHandler(IObjectToStringConverter<T> converter) {
			m_fieldConverter = converter;
		}

		@Nonnull @Override public List<T> queryFromString(@Nonnull String input, int max) throws Exception {
			return findMatchesUsingConverter(input, max, m_fieldConverter);
		}

		@Override public void onSelect(@Nonnull T instance) throws Exception {
			selected(instance);
		}

		@Override public void onEnter(@Nonnull String value) throws Exception {
			enterPressed(value);
		}
	}
}
