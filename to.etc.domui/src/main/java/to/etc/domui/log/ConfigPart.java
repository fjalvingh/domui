package to.etc.domui.log;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.ntbl.ExpandingEditTable;
import to.etc.domui.component.ntbl.IRowEditorEvent;
import to.etc.domui.component.ntbl.IRowEditorFactory;
import to.etc.domui.component.tbl.BasicRowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.log.data.Handler;
import to.etc.domui.log.data.HandlerType;
import to.etc.domui.log.tailer.ServerLogPage;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.handler.EtcLogFormat;
import to.etc.webapp.nls.BundleRef;

import javax.annotation.Nonnull;
import java.util.List;

public class ConfigPart extends Div {
	protected static final BundleRef BUNDLE = Msgs.BUNDLE;

	private final List<Handler> m_handlers;

	private ExpandingEditTable<Handler> m_table;

	private IRowEditorEvent<Handler, HandlerRowEditor> m_rowChangeListener;

	private SimpleListModel<Handler> m_model;

	private final String[] m_cols = new String[]{Handler.pTYPE, Handler.pFILE, Handler.pFORMAT};

	private ButtonBar m_buttonBar;

	public ConfigPart(@Nonnull List<Handler> handlers) {
		m_handlers = handlers;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		createButtonBar();
		createButtons();

		m_model = new SimpleListModel<Handler>(m_handlers);

		BasicRowRenderer<Handler> rr = new BasicRowRenderer<Handler>(Handler.class, m_cols);
		rr.addColumns("", "^follow", "%10", new IRenderInto<Handler>(){
			@Override
			public void render(@Nonnull NodeContainer node, @Nonnull final Handler handler) throws Exception {
				if (handler != null && handler.getType() == HandlerType.FILE){
					node.add(new LinkButton("follow", new IClicked<LinkButton>(){
						@Override
						public void clicked(@Nonnull LinkButton clickednode) throws Exception {
							ServerLogPage.moveSub(constructLogPath(handler.getFile()));
						}
					}));
				}
			}
		});
		m_table = new ExpandingEditTable<Handler>(Handler.class, m_model, rr);
		m_table.setNewAtStart(true);
		m_table.setEnableDeleteButton(true);
		m_table.setEnableExpandItems(true);
		m_table.setOnRowChangeCompleted(getRowChangeListener());

		m_table.setEditorFactory(new IRowEditorFactory<Handler, HandlerRowEditor>() {
			@Override
			public @Nonnull
			HandlerRowEditor createRowEditor(@Nonnull Handler instance, boolean isnew, boolean isReadonly) throws Exception {
				return new HandlerRowEditor(instance, m_table);
			}
		});

		add(m_table);
	}

	protected String constructLogPath(@Nonnull String fileName) {
		return EtcLoggerFactory.getSingleton().composeFullLogFileName(fileName);
	}

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
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				m_table.addNew(initializeNewInstance());
			}
		});
	}

	protected @Nonnull
	Handler initializeNewInstance() {
		Handler handler = new Handler(HandlerType.FILE, "logger1");
		handler.setFormat(EtcLogFormat.DEFAULT);
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
