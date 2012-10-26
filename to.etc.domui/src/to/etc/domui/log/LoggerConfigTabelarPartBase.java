package to.etc.domui.log;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.log.data.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public abstract class LoggerConfigTabelarPartBase<T extends LoggerDefBase> extends LoggerConfigPartBase {
	private final Class<T> m_type;

	private String[] m_cols;

	private ExpandingEditTable<T> m_table;

	private ButtonBar m_buttonBar;

	protected static final BundleRef BUNDLE = Msgs.BUNDLE;

	private Comparator<T> m_comparator;

	private IRowEditorEvent<T, LoggerRowEditor<T>> m_rowChangeListener;

	private SimpleListModel<T> m_model;

	protected LoggerConfigTabelarPartBase(@Nonnull Class<T> type) {
		super();
		m_type = type;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		createButtonBar();
		createButtons();

		m_model = new SimpleListModel<T>(getData());
		m_model.setComparator(getComparator());

		BasicRowRenderer<T> rr = new BasicRowRenderer<T>(m_type, getCols());
		m_table = new ExpandingEditTable<T>(m_type, m_model, rr);
		m_table.setNewAtStart(true);
		m_table.setEnableDeleteButton(true);
		m_table.setEnableExpandItems(true);
		m_table.setErrorFence(null);
		m_table.setOnRowChangeCompleted(getRowChangeListener());

		m_table.setEditorFactory(new IRowEditorFactory<T, LoggerRowEditor<T>>() {
			@Override
			public @Nonnull
			LoggerRowEditor<T> createRowEditor(@Nonnull T instance, boolean isnew, boolean isReadonly) throws Exception {
				return new LoggerRowEditor<T>(instance, m_table, getCols());
			}
		});

		add(m_table);
	}

	private Comparator<T> getComparator() {
		if(m_comparator == null) {
			m_comparator = createComparator();
		}
		return m_comparator;
	}

	protected Comparator<T> createComparator() {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if(o1 == null || o1.getKey() == null) {
					return -1;
				} else if(o2 == null || o2.getKey() == null) {
					return 1;
				}
				return o1.getKey().compareTo(o2.getKey());
			}
		};
	}

	protected abstract List<T> getData();

	protected String[] getCols() {
		if(m_cols == null) {
			m_cols = getDisplayCols();
		}
		return m_cols;
	}

	protected abstract String[] getDisplayCols();

	protected void createButtonBar() {
		add(getButtonBar());
	}

	public @Nonnull
	ButtonBar getButtonBar() {
		if(m_buttonBar == null) {
			m_buttonBar = new ButtonBar();
		}
		return m_buttonBar;
	}

	protected void createButtons() throws Exception {
		createAddButton();
	}

	private void createAddButton() {
		getButtonBar().addButton(BUNDLE.getString(Msgs.LOOKUP_FORM_NEW), "THEME/btnNew.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				m_table.addNew(initializeNewInstance());
			}
		});
	}

	protected @Nonnull
	abstract T initializeNewInstance();

	protected IRowEditorEvent<T, LoggerRowEditor<T>> getRowChangeListener() {
		if(m_rowChangeListener == null) {
			m_rowChangeListener = createBaseRowChangeListener();
		}
		return m_rowChangeListener;
	}

	protected void setRowChangeListener(IRowEditorEvent<T, LoggerRowEditor<T>> rowChangeListener) {
		m_rowChangeListener = rowChangeListener;
	}

	protected SimpleListModel<T> getModel() {
		return m_model;
	}
	
	protected IRowEditorEvent<T, LoggerRowEditor<T>> createBaseRowChangeListener() {
		return new IRowEditorEvent<T, LoggerRowEditor<T>>() {

			@Override
			public boolean onRowChanged(@Nonnull TableModelTableBase<T> model, @Nonnull LoggerRowEditor<T> editor, @Nonnull T instance, boolean isNew) throws Exception {
				if(MetaManager.hasDuplicates(model.getModel().getItems(0, model.getModel().getRows()), instance, LoggerDefBase.pKEY)) {
					editor.setMessage(UIMessage.error(LoggerDefBase.pKEY, Msgs.BUNDLE, Msgs.V_INVALID));
					return false;
				}
				return true;
			}
		};
	}
	
}
