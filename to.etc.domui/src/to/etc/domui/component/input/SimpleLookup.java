package to.etc.domui.component.input;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Represents simple lookup dialog that enables single item selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 3, 2010
 */

public class SimpleLookup<T> extends FloatingWindow {
	public interface IValueSelected<T> {
		void valueSelected(T value) throws Exception;
	}
	/**
	 * The result class. For Java classes this usually also defines the metamodel to use; for generic meta this should
	 * be the value record class type.
	 */
	final private Class<T> m_lookupClass;

	/**
	 * The metamodel to use to handle the data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	final private ClassMetaModel m_metaModel;

	private LookupForm<T> m_externalLookupForm;

	DataTable<T> m_result;

	private IQueryManipulator<T> m_queryManipulator;

	private IQueryHandler<T> m_queryHandler;

	private String m_lookupTitle;

	private String[] m_resultColumns;

	private IErrorMessageListener m_customErrorMessageListener;

	private boolean m_allowEmptyQuery;

	private IValueSelected<T> m_onValueSelected;

	private boolean m_renderAsCollapsed;

	/* temporary solution to allow use of same test IDs as it was used withing LookupInput. */
	private boolean m_usedWithinLookupInput;

	public SimpleLookup(Class<T> lookupClass, ClassMetaModel metaModel, String[] resultColumns) {
		this(lookupClass, metaModel);
		m_resultColumns = resultColumns;
	}

	public SimpleLookup(Class<T> lookupClass, String[] resultColumns) {
		this(lookupClass, (ClassMetaModel) null);
		m_resultColumns = resultColumns;
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param lookupClass
	 */
	public SimpleLookup(Class<T> lookupClass) {
		this(lookupClass, (ClassMetaModel) null);
	}

	public SimpleLookup(Class<T> lookupClass, ClassMetaModel metaModel) {
		super(true, null);
		m_lookupClass = lookupClass;
		m_metaModel = metaModel != null ? metaModel : MetaManager.findClassMeta(lookupClass);
	}


	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	public ClassMetaModel getMetaModel() {
		return m_metaModel;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		setWindowTitle(getLookupTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL) : getLookupTitle());

		setHeight("90%");
		setIcon("THEME/btnFind.png");
		if(getTestID() == null) {
			setTestID("simpleLookup");
		}

		//in case when external error message listener is set
		if(m_customErrorMessageListener != null && m_customErrorMessageListener instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) m_customErrorMessageListener);
			DomUtil.getMessageFence(this).addErrorListener(m_customErrorMessageListener);
		}
		LookupForm<T> lf = getExternalLookupForm() != null ? getExternalLookupForm() : new LookupForm<T>(getLookupClass(), getMetaModel());

		lf.setRenderAsCollapsed(m_renderAsCollapsed);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		add(lf);
		setOnClose(new IClicked<FloatingWindow>() {
			@Override
			public void clicked(FloatingWindow b) throws Exception {
				clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				m_result = null;
			}
		});

		lf.setClicked(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(LookupForm<T> b) throws Exception {
				closePressed();
			}
		});
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
			if(c == null) {
				//in case of cancelled search by query manipulator return null
				return;
			}
		}
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!c.hasRestrictions() && !isAllowEmptyQuery()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) {
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage().getConversation());
			model = new SimpleSearchModel<T>(src, qc);
		} else {
			model = new SimpleSearchModel<T>(m_queryHandler, qc);
		}
		setResultModel(model);
	}

	private void setResultModel(ITableModel<T> model) {
		if(m_result == null) {
			//-- We do not yet have a result table -> create one.
			SimpleRowRenderer<T> rr = null;
			if(m_resultColumns != null) {
				rr = new SimpleRowRenderer<T>(getLookupClass(), getMetaModel(), m_resultColumns);
			} else {
				rr = new SimpleRowRenderer<T>(getLookupClass(), getMetaModel());
			}

			m_result = new DataTable<T>(model, rr);
			add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				@Override
				public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
					clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					close();
					if(getOnValueSelected() != null) {
						getOnValueSelected().valueSelected(val);
					}
				}
			});

			//-- Add the pager,
			DataPager pg = new DataPager(m_result);
			add(pg);
		} else {
			m_result.setModel(model); // Change the model
		}
		if(isUsedWithinLookupInput()) {
			m_result.setTestID("resultTableLookupInput");
		} else {
			m_result.setTestID("resultTableSimpleLookup");
		}
	}

	@Override
	public void closePressed() throws Exception {
		super.closePressed();
		if(getOnValueSelected() != null) {
			getOnValueSelected().valueSelected(null);
		}
	}

	public void startLookup(NodeBase parent, IValueSelected<T> callback) {
		setOnValueSelected(callback);
		UrlPage body = parent.getPage().getBody();
		body.add(this);
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 * @return
	 */
	public IQueryManipulator<T> getQueryManipulator() {
		return m_queryManipulator;
	}

	/**
	 * The query handler to use, if a special one is needed. The default query handler will use the
	 * normal conversation-associated DataContext to issue the query.
	 * @return
	 */
	public IQueryHandler<T> getQueryHandler() {
		return m_queryHandler;
	}

	public void setQueryHandler(IQueryHandler<T> queryHandler) {
		m_queryHandler = queryHandler;
	}

	/**
	 * When set this defines the title of the lookup window.
	 * @return
	 */
	public String getLookupTitle() {
		return m_lookupTitle;
	}

	/**
	 * When set this defines the title of the lookup window.
	 *
	 * @param lookupTitle
	 */
	public void setLookupTitle(String lookupTitle) {
		m_lookupTitle = lookupTitle;
	}

	/**
	 * When T the user can press search even when no criteria are entered.
	 * @return
	 */
	public boolean isAllowEmptyQuery() {
		return m_allowEmptyQuery;
	}

	public void setAllowEmptyQuery(boolean allowEmptyQuery) {
		m_allowEmptyQuery = allowEmptyQuery;
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 *
	 * @param queryManipulator
	 */
	public void setQueryManipulator(IQueryManipulator<T> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	public LookupForm<T> getExternalLookupForm() {
		return m_externalLookupForm;
	}

	public void setExternalLookupForm(LookupForm<T> externalLookupForm) {
		m_externalLookupForm = externalLookupForm;
	}

	public String[] getResultColumns() {
		return m_resultColumns;
	}

	public void setResultColumns(String[] resultColumns) {
		m_resultColumns = resultColumns;
	}

	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	protected IValueSelected<T> getOnValueSelected() {
		return m_onValueSelected;
	}

	private void setOnValueSelected(IValueSelected<T> onValueSelected) {
		m_onValueSelected = onValueSelected;
	}

	public boolean isUsedWithinLookupInput() {
		return m_usedWithinLookupInput;
	}

	public void setUsedWithinLookupInput(boolean usedWithinLookupInput) {
		m_usedWithinLookupInput = usedWithinLookupInput;
	}

	public boolean isRenderAsCollapsed() {
		return m_renderAsCollapsed;
	}

	public void setRenderAsCollapsed(boolean renderAsCollapsed) {
		m_renderAsCollapsed = renderAsCollapsed;
	}
}
