package to.etc.domui.component.input;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component2.lookupinput.SelectOnePanel;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IForTarget;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for search-as-you-type input controls. This is not itself a control but forms the
 * basis for things that are.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 24, 2018
 */
abstract public class SearchAsYouTypeBase<T> extends Div implements IForTarget {
	static protected final int MAX_RESULTS = 7;

	static private final Set<Class<?>> SIMPLECLASSES = new HashSet<Class<?>>(Arrays.asList(String.class, Date.class, Integer.class, int.class, Long.class, long.class));

	@Nonnull final private ClassMetaModel m_dataModel;

	@Nonnull final private Class<T> m_actualType;

	/** The base string to use for all CSS classes. */
	@Nonnull
	private String m_cssBase;

	private Img m_imgWaiting = new Img("THEME/lui-keyword-wait.gif");

	private NodeContainer m_resultMessageContainer;

	private int m_lastResultCount = -1;

	private boolean m_addSingleMatch;

	@Nullable
	private IRenderInto<T> m_rowRenderer;

	private Input m_input = new Input();

	@Nullable
	private SelectOnePanel<T> m_selectPanel;

	/**
	 * Called with input, required to either accept the data or return a list of choices.
	 */
	@Nullable
	protected abstract List<T> onLookupTyping(@Nonnull String curdata, boolean done) throws Exception;

	/**
	 * Called when a value is selected from the dropdown.
	 */
	protected abstract void onRowSelected(@Nonnull T value) throws Exception;

	protected abstract IRenderInto<T> getActualRenderer() throws Exception;

	@DefaultNonNull
	public final static class Result<T> {
		private final List<T> m_list;

		@Nullable
		private final T m_match;

		public Result(List<T> list, @Nullable T match) {
			m_list = list;
			m_match = match;
		}

		public List<T> getList() {
			return m_list;
		}

		@Nullable
		public T getMatch() {
			return m_match;
		}
	}

	/**
	 * Create a control for the specified type.
	 */
	public SearchAsYouTypeBase(String cssBase, @Nonnull Class<T> clz) {
		m_cssBase = cssBase;
		m_actualType = clz;
		m_dataModel = MetaManager.findClassMeta(clz);
	}

	public SearchAsYouTypeBase<T> setCssBase(@Nonnull String cssBase) {
		m_cssBase = cssBase;
		return this;
	}

	/**
	 * Called when the input is/becomes empty during typing.
	 */
	protected void onEmptyInput(boolean done) throws Exception {
	}

	@Override
	public void createContent() throws Exception {
		addCssClass(getCssBase());

		m_imgWaiting.setCssClass(cssBase("waiting"));
		m_imgWaiting.setDisplay(DisplayType.NONE);
		add(m_imgWaiting);
		m_input.addCssClass(cssBase("keyword"));
		m_input.setMaxLength(40);
		m_input.setSize(14);
		add(m_input);

		//m_input.setOnLookupTyping((component, done) -> handleLookupTyping(done));
		appendCreateJS("new WebUI.SearchPopup('" + getActualID() + "','" + m_input.getActualID() + "');");
	}

	/**
	 * Sent regularly whenever the search box is typed in. Causes a ValueChanged event which can then do
	 * whatever lookup is needed.
	 * @param ctx
	 * @throws Exception
	 */
	public void webActionlookupTyping(IRequestContext ctx) throws Exception {
		handleLookupTyping(false);
	}

	/**
	 * Send when return is pressed in the search box. Should finalize the selected value, if
	 * one is present.
	 * @param ctx
	 * @throws Exception
	 */
	public void webActionlookupTypingDone(IRequestContext ctx) throws Exception {
		handleLookupTyping(true);
	}


	/**
	 * Called on keyboard entry to handle querying or return presses.
	 */
	private void handleLookupTyping(boolean done) throws Exception {
		//-- If input is empty just clear all presentation but do not call any handler.
		String curdata = m_input.getRawValue();
		if(curdata == null || curdata.length() == 0) {
			onEmptyInput(done);
			showResults(null);
			return;
		}

		//-- Ask the base class for a result
		List<T> list = onLookupTyping(curdata, done);
		showResults(list);
	}

	/**
	 * Show the results of a lookup query. The parameter can have the following values:
	 * <ul>
	 * <li>null: this clears all search presentation; it means that a query is not necessary/possible. It does <b>not</b> mean
	 * that there are no results!</li>
	 * <li>empty list: indicates that a query returned no results. This will cause the control to display the "no query results" presentation.</li>
	 * <li>List with too many items: if the list contains more that max items the control will show the "too many results" presentation.</li>
	 * <li>List with &lt;= max items: all of the items will be shown in a selection popup; the user can select one with mouse or keyboard.</li>
	 * </ul>
	 */
	private void showResults(@Nullable List<T> isl) throws Exception {
		if(null == isl) {
			//-- Null means: there is no query entered at all. Remove both popup and message panels.
			clearResultMessage();
			clearResultPopup();
			return;
		}

		int rc = isl.size();
		if(rc == 0) {
			if(m_lastResultCount == 0)
				return;
			setResultMessage("no-res", Msgs.BUNDLE.getString(Msgs.UI_KEYWORD_SEARCH_NO_MATCH));
			return;
		}
		if(rc > MAX_RESULTS) {
			if(m_lastResultCount > MAX_RESULTS)
				return;
			setResultMessage("keyword-large", Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_LARGE_MATCH, "" + MAX_RESULTS));
			return;
		}
		openDropdown(isl);
	}

	private void openDropdown(@Nonnull List<T> list) throws Exception {
		clearResultMessage();
		clearResultPopup();
		SelectOnePanel<T> pnl = m_selectPanel = new SelectOnePanel<T>(list, getActualRenderer());
		add(pnl);

		pnl.setOnValueChanged((IValueChanged<SelectOnePanel<T>>) component -> {
			clearResultMessage();
			clearResultPopup();
			T selection = component.getValue();
			if(null != selection)
				onRowSelected(selection);
		});

		pnl.setClicked(clickednode -> {
			//we just need to deliver selected value here, that is why we have empty click handler
		});
	}

	protected Input getInput() {
		return m_input;
	}

	protected String getCssBase() {
		return m_cssBase;
	}

	protected String cssBase(String s) {
		if(s.length() == 0)
			return m_cssBase;
		return m_cssBase + "-" + s;
	}

	/**
	 * T if the actual type is a simple type like String or numeric.
	 */
	protected boolean isSimpleType() {
		return SIMPLECLASSES.contains(m_actualType);
	}

	//private void handleSelectValueFromPopup(T val) throws Exception {
	//	System.out.println("GOT: " + val);
	//	if(null != m_handler) {
	//		m_handler.onSelect(val);
	//	}
	//
	//	clearResultMessage();
	//	clearResultPopup();
	//	//m_input.setRawValue("");
	//	m_input.setFocus();
	//}

	protected void clearAllExtras() {
		clearResultPopup();
		clearResultMessage();
	}

	protected void clearResultPopup() {
		SelectOnePanel<T> selectPanel = m_selectPanel;
		if(null != selectPanel) {
			if(selectPanel.isAttached())
				selectPanel.remove();
			m_selectPanel = null;
		}
	}

	/**
	 * Set a result count indicator field, using the specified text and the specified css class. If
	 * the field is already present it is updated, else it is created.
	 */
	private void setResultMessage(String css, String text) {
		if(m_resultMessageContainer == null)
			m_resultMessageContainer = new Span();
		m_resultMessageContainer.setCssClass(cssBase(css));
		m_resultMessageContainer.setText(text);
		if(!m_resultMessageContainer.isAttached())
			add(m_resultMessageContainer);
		clearResultPopup();
	}

	protected void clearResultMessage() {
		if(m_resultMessageContainer != null && m_resultMessageContainer.isAttached())
			m_resultMessageContainer.remove();
	}

	public boolean isAddSingleMatch() {
		return m_addSingleMatch;
	}

	public void setAddSingleMatch(boolean addSingleMatch) {
		m_addSingleMatch = addSingleMatch;
	}

	@Nonnull public Class<T> getActualType() {
		return m_actualType;
	}

	@Nonnull public ClassMetaModel getDataModel() {
		return m_dataModel;
	}

	public boolean isReadOnly() {
		return m_input.isReadOnly();
	}

	public void setReadOnly(boolean ro) {
		m_input.setReadOnly(ro);
	}

	public boolean isDisabled() {
		return m_input.isDisabled();
	}

	public void setDisabled(boolean d) {
		m_input.setDisabled(d);
	}
	@Nullable @Override public NodeBase getForTarget() {
		return m_input;
	}

	@Nullable @Override protected String getFocusID() {
		return m_input.getActualID();
	}

}
