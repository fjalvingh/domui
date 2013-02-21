package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This class is an &lt;input&gt; control which can do on-the-fly lookup of data that is being typed. How
 * the lookup is done is fully transparant: the method {@link IQuery#queryFromString(String, int)} in a
 * handler interface {@link IQuery} is called, and it should return the results to show in the popup box.
 * The method gets passed the partially typed input string plus a maximal #of results to return.
 *
 * <p>The control handles a specific type T, which stands for the type of object being searched for by
 * this control.</p>
 *
 * <p>If the string entered by this control is just finished and enter is pressed then this control fires
 * an {@link IQuery#onEnter(String)} event with the entered string as parameter. This can then be used to
 * either locate the specific instance or a new instance can be added for this string. This means that
 * entering a string and pressing return will <b>not</b> automatically select a result from the list, if
 * shown.</p>
 * <p>If the user selects one of the results as returned by the {@link IQuery#queryFromString(String, int)}
 * method then the control will fire the {@link IQuery#onSelect(T)} event with the selected instance.</p>
 *
 * <p>In both of these cases the input area of the control will be cleared, and any popup will be removed.
 * The control will be ready for another lookup/input action.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 9, 2011
 */
public class SearchInput<T> extends Div {
	static private final int MAX_RESULTS = 7;

	@Nonnull
	final private ClassMetaModel m_dataModel;

	@Nonnull
	final private Class<T>		m_dataClass;

	final private Object[] m_columns;

	/**
	 * Inner interface to define the query to execute to lookup data, and the handlers for
	 * completion events. Users of this control must define a handler and pass it to the
	 * control to make it work.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Aug 9, 2011
	 */
	public interface IQuery<T> {
		/**
		 * This gets called when it is time to lookup something. The string entered in
		 * the text field is passed. This method must return the result as a list; the list
		 * should be at most (max+1) elements big; in that case the code will show "too
		 * many results". If the list is empty it will show "no results", in all other cases
		 * it will display the result's rows to select from.
		 *
		 * as an indicator.
		 * @param input
		 * @return
		 * @throws Exception
		 */
		List<T>	queryFromString(String input, int max) throws Exception;

		/**
		 * When a literal value in the result combo is selected this will be called
		 * with that literal value. At that point the input box for this control will
		 * already have been cleared.
		 *
		 * @param instance
		 * @throws Exception
		 */
		void		onSelect(T instance) throws Exception;

		/**
		 * When a value is entered and ENTER is pressed in the input box this gets
		 * called with the literal string entered. It can be used to either create
		 * or select some value. When called it should return true if the input box
		 * is to be cleared.
		 * @param value
		 * @throws Exception
		 */
		void		onEnter(String value) throws Exception;
	}

	@Nullable
	private IQuery<T> m_handler;

	private Img m_imgWaiting = new Img("THEME/lui-keyword-wait.gif");

	private Div m_pnlSearchPopup;

	private NodeContainer	m_resultMessageContainer;

	private int m_lastResultCount = -1;

	private Input		m_input = new Input();

	/**
	 * Create a control for the specified type, and show the specified properties in the popup list. This
	 * constructor creates an <b>incomplete</b> control: you must call {@link #setHandler(IQuery)} to completely
	 * define the control or use the {@link #SearchInput(IQuery, Class, String...)} constructor.
	 *
	 * @param clz
	 * @param columns
	 */
	public SearchInput(@Nonnull Class<T> clz, Object... columns) {
		this(null, clz, columns);
	}

	/**
	 * Create a control for the specified type, using the handler to query and handle events.
	 * @param handler	The IQUery instance which handles queries and accepts events.
	 * @param clz		The data class to display/handle
	 * @param columns	The property names to show in the popup window.
	 */
	public SearchInput(IQuery<T> handler, @Nonnull Class<T> clz, Object... columns) {
		m_handler = handler;
		m_dataClass = clz;
		m_columns = columns;
		m_dataModel = MetaManager.findClassMeta(clz);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-qsi");

		//		setPosition(PositionType.RELATIVE);
		//		setDisplay(DisplayType.INLINE_BLOCK);
		m_imgWaiting.setCssClass("ui-lui-waiting");
		m_imgWaiting.setDisplay(DisplayType.NONE);
		add(m_imgWaiting);
		m_input.setCssClass("ui-lui-keyword");
		m_input.setMaxLength(40);
		m_input.setSize(14);
		add(m_input);

		m_input.setOnLookupTyping(new ILookupTypingListener<NodeBase>() {
			@Override
			public void onLookupTyping(@Nonnull NodeBase component, boolean done) throws Exception {
				handleLookupTyping(done);
			}
		});
	}

	/**
	 * Called on keyboard entry to handle querying or return presses.
	 * @param done
	 * @throws Exception
	 */
	private void	handleLookupTyping(boolean done) throws Exception {
		//-- If input is empty just clear all presentation but do not call any handler.
		String curdata = m_input.getRawValue();
		if(curdata.length() == 0) {
			showResults(null);
			return;
		}

		//-- If just enter is pressed-> call handler and be done.
		if(done) {
			if(m_handler != null) {
				m_handler.onEnter(curdata);
			}
			clearResultPopup();
			clearResultMessage();
			m_input.setRawValue("");
			return;
		}

		//-- We need to do a query.. Ask the handler for a result
		List<T>	res = null;
		if(m_handler != null) {
			res = m_handler.queryFromString(curdata, MAX_RESULTS);
		}
		showResults(res);
	}

	/**
	 * Show the results of a lookup query. The parameter can have the following values:
	 * <ul>
	 *	<li>null: this clears all search presentation; it means that a query is not necessary/possible. It does <b>not</b> mean
	 *		that there are no results!</li>
	 *	<li>empty list: indicates that a query returned no results. This will cause the control to display the "no query results" presentation.</li>
	 *	<li>List with too many items: if the list contains more that max items the control will show the "too many results" presentation.</li>
	 *	<li>List with &lt;= max items: all of the items will be shown in a selection popup; the user can select one with mouse or keyboard.</li>
	 * </ul>
	 *
	 * @param isl
	 * @throws Exception
	 */
	private void showResults(@Nullable List<T> isl) throws Exception {
		if(null == isl) {
			//-- Null means: there is no query entered at all. Remove both popup and message panels.
			clearResultMessage();
			clearResultPopup();
			return;
		}

		int rc = isl.size();
		System.out.println("search: count="+rc);
		if(rc == 0) {
			if(m_lastResultCount == 0)
				return;
			setResultMessage("ui-lui-keyword-no-res", Msgs.BUNDLE.getString(Msgs.UI_KEYWORD_SEARCH_NO_MATCH));
			return;
		}
		if(rc > MAX_RESULTS) {
			if(m_lastResultCount > MAX_RESULTS)
				return;
			setResultMessage("ui-lui-keyword-large", Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_LARGE_MATCH, "" + MAX_RESULTS));
			return;
		}
		clearResultMessage();

		//-- Create the result list popup.
		if(m_pnlSearchPopup == null)
			m_pnlSearchPopup = new Div();
		else
			m_pnlSearchPopup.removeAllChildren();
		if(!m_pnlSearchPopup.isAttached()) {
			add(m_pnlSearchPopup);
			m_pnlSearchPopup.setCssClass("ui-lui-keyword-popup");
			m_pnlSearchPopup.setPosition(PositionType.ABSOLUTE);
			m_pnlSearchPopup.setZIndex(10);
		}

		SimpleListModel<T>	mdl = new SimpleListModel<T>(isl);
		KeyWordPopupRowRenderer<T> rr = new KeyWordPopupRowRenderer<T>(m_dataModel);
		if(m_columns != null)
			rr.addColumns(m_columns);
		rr.setRowClicked(new ICellClicked<T>() {
			@Override
			public void cellClicked(@Nonnull NodeBase tr, @Nonnull T val) throws Exception {
				handleSelectValueFromPopup(val);
			}
		});
		DataTable<T> tbl = new DataTable<T>(mdl, rr);
		m_pnlSearchPopup.add(tbl);
		tbl.setWidth("100%");
		tbl.setOverflow(Overflow.HIDDEN);
		tbl.setPosition(PositionType.RELATIVE);
	}

	private void handleSelectValueFromPopup(T val) throws Exception {
		System.out.println("GOT: "+val);
		if(null != m_handler) {
			m_handler.onSelect(val);
		}

		clearResultMessage();
		clearResultPopup();
		m_input.setRawValue("");
	}

	private void clearResultPopup() {
		if(null != m_pnlSearchPopup && m_pnlSearchPopup.isAttached())
			m_pnlSearchPopup.remove();
	}

	/**
	 * Set a result count indicator field, using the specified text and the specified css class. If
	 * the field is already present it is updated, else it is created.
	 * @param css
	 * @param text
	 */
	private void setResultMessage(String css, String text) {
		if(m_resultMessageContainer == null)
			m_resultMessageContainer = new Span();
		m_resultMessageContainer.setCssClass(css);
		m_resultMessageContainer.setText(text);
		if(!m_resultMessageContainer.isAttached())
			add(m_resultMessageContainer);
		clearResultPopup();
	}

	private void	clearResultMessage() {
		if(m_resultMessageContainer != null && m_resultMessageContainer.isAttached())
			m_resultMessageContainer.remove();
	}

	/**
	 * Get the current query/event handler for this control.
	 * @return
	 */
	public IQuery<T> getHandler() {
		return m_handler;
	}

	public void setHandler(IQuery<T> handler) {
		m_handler = handler;
	}
}
