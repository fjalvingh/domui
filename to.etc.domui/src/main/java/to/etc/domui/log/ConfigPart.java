package to.etc.domui.log;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.ntbl.ExpandingEditTable;
import to.etc.domui.component.ntbl.IRowEditorEvent;
import to.etc.domui.component.ntbl.IRowEditorFactory;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.log.data.Handler;
import to.etc.domui.log.data.HandlerType;
import to.etc.domui.log.tailer.ServerLogPage;
import to.etc.domui.util.Msgs;
import to.etc.log.EtcLoggerFactory;
import to.etc.webapp.nls.BundleRef;

import java.util.List;

public class ConfigPart extends Div {
	protected static final BundleRef BUNDLE = Msgs.BUNDLE;

	private final List<Handler> m_handlers;

	private ExpandingEditTable<Handler> m_table;

	private IRowEditorEvent<Handler, HandlerRowEditor> m_rowChangeListener;

	private SimpleListModel<Handler> m_model;

	private final String[] m_cols = new String[]{Handler.pTYPE, Handler.pFILE, Handler.pFORMAT};

	private ButtonBar m_buttonBar;

	public ConfigPart(@NonNull List<Handler> handlers) {
		m_handlers = handlers;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		createButtonBar();
		createButtons();

		m_model = new SimpleListModel<Handler>(m_handlers);

		RowRenderer<Handler> rr = new RowRenderer<Handler>(Handler.class);
		rr.column(Handler.pTYPE);
		rr.column(Handler.pFILE);
		rr.column(Handler.pFORMAT);

		rr.column().label("^follow").width(20).renderer((node, handler) -> {
			if(handler.getType() == HandlerType.FILE) {
				node.add(new LinkButton("follow", clickednode -> ServerLogPage.moveSub(constructLogPath(handler.getFile()))));
			}
		});
		m_table = new ExpandingEditTable<Handler>(Handler.class, m_model, rr);
		m_table.setNewAtStart(true);
		m_table.setEnableDeleteButton(true);
		m_table.setEnableExpandItems(true);
		m_table.setOnRowChangeCompleted(getRowChangeListener());

		m_table.setEditorFactory((IRowEditorFactory<Handler, HandlerRowEditor>) (instance, isnew, isReadonly) -> new HandlerRowEditor(instance, m_table));

		add(m_table);
	}

	protected String constructLogPath(@NonNull String fileName) {
		return EtcLoggerFactory.getSingleton().composeFullLogFileName(fileName);
	}

	protected void createButtonBar() {
		add(getButtonBar());
	}

	@NonNull
	public ButtonBar getButtonBar() {
		if(m_buttonBar == null) {
			m_buttonBar = new ButtonBar();
		}
		return m_buttonBar;
	}

	protected void createButtons() throws Exception {
		createAddButton();
	}

	private void createAddButton() {
		getButtonBar().addButton(BUNDLE.getString(Msgs.LOOKUP_FORM_NEW), Icon.of("THEME/btnNew.png"), new IClicked<DefaultButton>() {
			@Override
			public void clicked(@NonNull DefaultButton b) throws Exception {
				m_table.addNew(initializeNewInstance());
			}
		});
	}

	@NonNull
	protected Handler initializeNewInstance() {
		Handler handler = new Handler(HandlerType.FILE, "logger1");
		return handler;
	}

	protected IRowEditorEvent<Handler, HandlerRowEditor> getRowChangeListener() {
		return m_rowChangeListener;
	}

	protected void setRowChangeListener(IRowEditorEvent<Handler, HandlerRowEditor> rowChangeListener) {
		m_rowChangeListener = rowChangeListener;
	}

	public boolean validateData() {
		// TODO Auto-generated method stub
		return true;
	}

}
