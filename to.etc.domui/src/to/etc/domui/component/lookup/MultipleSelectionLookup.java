package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.LookupForm.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Dialog that enables multiple lookup selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2009
 */
public class MultipleSelectionLookup<T> extends FloatingWindow {
	static final private int WIDTH = 740;

	private Class<T> m_lookupClass;

	/**
	 * The metamodel to use to handle the data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	final private ClassMetaModel m_metaModel;

	private LookupForm<T> m_externalLookupForm;

	List<T> m_selectionResult;

	List<T> m_queryResult;

	MultipleSelectionDataTable<T> m_queryResultTable;

	Table m_resultTable;

	String m_title;

	IMultiSelectionResult<T> m_onReceiveResult;

	private IQueryHandler<T> m_queryHandler;

	private IQueryManipulator<T> m_queryManipulator;

	private IErrorMessageListener m_customErrorMessageListener;

	private String[] m_resultColumns = new String[0];

	public MultipleSelectionLookup(Class<T> lookupClass, ClassMetaModel metaModel, boolean isModal, String title, IMultiSelectionResult<T> onReceiveResult) {
		super(isModal, title);
		m_lookupClass = lookupClass;
		m_metaModel = metaModel != null ? metaModel : MetaManager.findClassMeta(lookupClass);
		m_lookupClass = lookupClass;
		setCssClass("ui-fw");
		m_selectionResult = new ArrayList<T>();
		if(getWidth() == null) {
			setWidth(WIDTH + "px");
		}
		m_onReceiveResult = onReceiveResult;
	}

	public MultipleSelectionLookup(Class<T> lookupClass, boolean isModal, String title, IMultiSelectionResult<T> onReceiveResult) {
		this(lookupClass, (ClassMetaModel) null, isModal, title, onReceiveResult);
	}

	public void show(NodeBase parent) {
		UrlPage body = parent.getPage().getBody();
		body.add(this);
	}


	@Override
	public void createContent() throws Exception {
		super.createContent();
		setHeight("90%");
		setIcon("THEME/btnFind.png");
		setTestID("workorderSelectionDialog");

		//in case when external error message listener is set
		if(m_customErrorMessageListener != null && m_customErrorMessageListener instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) m_customErrorMessageListener);
			DomUtil.getMessageFence(this).addErrorListener(m_customErrorMessageListener);
		}

		LookupForm<T> lf = getExternalLookupForm() != null ? getExternalLookupForm() : new LookupForm<T>(getLookupClass(), getMetaModel());
		if(m_onReceiveResult != null) {
			//-- Add a "confirm" button to the lookup form
			DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CONFIRM));
			b.setIcon("THEME/btnConfirm.png");
			b.setTestID("confirmButton");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {
					close();
					m_onReceiveResult.onReturnResult((m_queryResultTable != null) ? m_queryResultTable.getAccumulatedResults() : Collections.EMPTY_LIST);
				}
			});
			lf.addButtonItem(b, 600, ButtonMode.BOTH);
		}
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		add(lf);
		setOnClose(new IClicked<FloatingWindow>() {
			public void clicked(FloatingWindow b) throws Exception {
				clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				if(m_onReceiveResult != null) {
					m_onReceiveResult.onReturnResult(Collections.EMPTY_LIST);
				}
			}
		});

		lf.setClicked(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				closePressed();
			}
		});
	}

	protected void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
		}

		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!c.hasRestrictions()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH));
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) throws Exception {
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage().getConversation());
			model = new SimpleSearchModel<T>(src, qc);
		} else {
			model = new SimpleSearchModel<T>(m_queryHandler, qc);
		}

		if(m_queryResultTable == null) {
			//-- We do not yet have a result table -> create one.
			MultipleSelectionRowRenderer<T> rr = new MultipleSelectionRowRenderer<T>(getLookupClass(), getMetaModel(), m_resultColumns) {
				@Override
				public int getRowWidth() {
					int pxw = DomUtil.pixelSize(getWidth());
					return (pxw == -1 ? WIDTH : pxw) - 4;
				}

				@Override
				public int getSelectionColWidth() {
					return 50;
				}
			};

			m_queryResultTable = new MultipleSelectionDataTable<T>(m_lookupClass, model, rr);
			add(m_queryResultTable);
			m_queryResultTable.setPageSize(10);
			m_queryResultTable.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
					m_queryResultTable.handleRowClicked(pg, tr, val);
				}
			});

			//-- Add the pager,
			DataPager pg = new DataPager(m_queryResultTable);
			add(pg);
		} else {
			m_queryResultTable.setModel(model); // Change the model
		}
		m_queryResultTable.setTestID("queryResultTable");
	}

	public LookupForm<T> getExternalLookupForm() {
		return m_externalLookupForm;
	}

	public void setExternalLookupForm(LookupForm<T> externalLookupForm) {
		m_externalLookupForm = externalLookupForm;
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public void setTitle(String title) {
		m_title = title;
	}

	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	public IQueryManipulator<T> getQueryManipulator() {
		return m_queryManipulator;
	}

	public void setQueryManipulator(IQueryManipulator<T> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	public ClassMetaModel getMetaModel() {
		return m_metaModel;
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
}
