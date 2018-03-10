package to.etc.domui.log;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.controlfactory.ModelBindings;
import to.etc.domui.component.input.TextStr;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.ntbl.ExpandingEditTable;
import to.etc.domui.component.ntbl.IEditor;
import to.etc.domui.component.ntbl.IRowEditorEvent;
import to.etc.domui.component.ntbl.IRowEditorFactory;
import to.etc.domui.component.tbl.BasicRowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.component.tbl.TableModelTableBase;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.css.VerticalAlignType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.log.data.Filter;
import to.etc.domui.log.data.Handler;
import to.etc.domui.log.data.HandlerType;
import to.etc.domui.log.data.Matcher;
import to.etc.domui.util.Msgs;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.EtcMDCAdapter;
import to.etc.log.handler.LogFilterType;
import to.etc.webapp.nls.BundleRef;

import javax.annotation.Nonnull;

public class HandlerRowEditor extends Div implements IEditor {
	protected static final BundleRef BUNDLE = Msgs.BUNDLE;

	private final Handler m_instance;

	private final TableModelTableBase<Handler> m_model;

	private ModelBindings m_bindings;

	private ExpandingEditTable<Matcher> m_tableMatchers;

	private SimpleListModel<Matcher> m_modelMatchers;

	private ExpandingEditTable<Filter> m_tableFilters;

	private SimpleListModel<Filter> m_modelFilters;

	private String m_formatHelp = null;

	@Override
	public boolean validate(boolean isnew) throws Exception {
		m_bindings.moveControlToModel();
		return true;
	}

	public HandlerRowEditor(Handler instance, TableModelTableBase<Handler> model) {
		super();
		m_instance = instance;
		m_model = model;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(new ErrorMessageDiv(this, true));
		Table layout = new Table();
		add(layout);
		TR row = layout.addBody().addRow();
		row.setVerticalAlign(VerticalAlignType.TOP);
		TD part1 = row.addCell();
		addHandlerPart(part1);
		TD part2 = row.addCell();
		part2.setPaddingLeft("10px");
		addMatcherPart(part2);
		TD part3 = row.addCell();
		part3.setPaddingLeft("10px");
		addFilterPart(part3);
	}

	private void addHandlerPart(NodeContainer container) throws Exception {
		FormBuilder fb = new FormBuilder(container);
		IControl<HandlerType> typeCtl = (IControl<HandlerType>) fb.property(m_instance, Handler.pTYPE).control();
		IControl<String> nameCtl = (IControl<String>) fb.property(m_instance, Handler.pFILE).control();

		TextStr formatCtl = new TextStr();
		formatCtl.setMaxLength(150);
		formatCtl.setSize(60);
		formatCtl.setTitle(getFormatHelp());
		fb.property(m_instance, Handler.pFORMAT).control(formatCtl);
		final TextNode holder = new TextNode(nameCtl.getValueSafe());
		typeCtl.setOnValueChanged((IValueChanged<NodeBase>) component -> updateNameByType(typeCtl, nameCtl, holder));
		if(HandlerType.STDOUT == typeCtl.getValue()) {
			updateNameByType(typeCtl, nameCtl, holder);
		}
	}

	private String getFormatHelp() {
		if(m_formatHelp == null) {
			m_formatHelp = $("format.help");
		}
		return m_formatHelp;
	}

	private void updateNameByType(final IControl<HandlerType> typeCtl, final IControl<String> nameCtl, final TextNode holder) {
		HandlerType type = typeCtl.getValue();
		nameCtl.setDisabled(type == HandlerType.STDOUT);
		if(type == HandlerType.STDOUT) {
			holder.setText(nameCtl.getValueSafe());
			nameCtl.setValue("console");
		} else {
			nameCtl.setValue(holder.getText());
		}
	}

	private void addMatcherPart(NodeContainer container) throws Exception {
		container.add(new LinkButton($("add.matcher"), new IClicked<LinkButton>() {

			@Override
			public void clicked(@Nonnull LinkButton clickednode) throws Exception {
				m_tableMatchers.addNew(new Matcher("", EtcLoggerFactory.getSingleton().getDefaultLevel()));
			}
		}));
		m_modelMatchers = new SimpleListModel<Matcher>(m_instance.getMatchers());
		final String[] cols = new String[]{Matcher.pNAME, Matcher.pLEVEL};

		BasicRowRenderer<Matcher> rr = new BasicRowRenderer<Matcher>(Matcher.class, cols);
		m_tableMatchers = new ExpandingEditTable<Matcher>(Matcher.class, m_modelMatchers, rr);
		m_tableMatchers.setNewAtStart(true);
		m_tableMatchers.setEnableDeleteButton(true);
		m_tableMatchers.setEnableExpandItems(true);
		m_tableMatchers.setOnRowChangeCompleted(new IRowEditorEvent<Matcher, RowEditorBase<Matcher>>() {

			@Override
			public boolean onRowChanged(@Nonnull TableModelTableBase<Matcher> tablecomponent, @Nonnull RowEditorBase<Matcher> editor, @Nonnull Matcher instance, boolean isNew) throws Exception {
				if(MetaManager.hasDuplicates(m_modelMatchers.getItems(0, m_modelMatchers.getRows()), instance, Matcher.pNAME)) {
					editor.setMessage(UIMessage.error(Matcher.pNAME, Msgs.BUNDLE, Msgs.V_INVALID_NOT_UNIQUE));
					return false;
				}
				return true;
			}
		});

		m_tableMatchers.setEditorFactory(new IRowEditorFactory<Matcher, RowEditorBase<Matcher>>() {
			@Override
			public @Nonnull
			RowEditorBase<Matcher> createRowEditor(@Nonnull Matcher instance, boolean isnew, boolean isReadonly) throws Exception {
				return new RowEditorBase<Matcher>(instance, m_tableMatchers, cols);
			}
		});

		container.add(m_tableMatchers);
	}

	private void addFilterPart(TD container) throws Exception {
		container.add(new LinkButton($("add.filter"), new IClicked<LinkButton>() {

			@Override
			public void clicked(@Nonnull LinkButton clickednode) throws Exception {
				m_tableFilters.addNew(new Filter(LogFilterType.MDC, EtcMDCAdapter.LOGINID, "USER1"));
			}
		}));
		m_modelFilters = new SimpleListModel<Filter>(m_instance.getFilters());
		final String[] cols = new String[]{Filter.pTYPE, Filter.pKEY, Filter.pVALUE};

		BasicRowRenderer<Filter> rr = new BasicRowRenderer<Filter>(Filter.class, cols);
		m_tableFilters = new ExpandingEditTable<Filter>(Filter.class, m_modelFilters, rr);
		m_tableFilters.setNewAtStart(true);
		m_tableFilters.setEnableDeleteButton(true);
		m_tableFilters.setEnableExpandItems(true);

		m_tableFilters.setOnRowChangeCompleted(new IRowEditorEvent<Filter, FilterRowEditor>() {

			@Override
			public boolean onRowChanged(@Nonnull TableModelTableBase<Filter> tablecomponent, @Nonnull FilterRowEditor editor, @Nonnull Filter instance, boolean isNew) throws Exception {
				if(MetaManager.hasDuplicates(m_modelFilters.getItems(0, m_modelFilters.getRows()), instance, Filter.pKEY)) {
					editor.setMessage(UIMessage.error(Filter.pKEY, Msgs.BUNDLE, Msgs.V_INVALID_NOT_UNIQUE));
					return false;
				}
				return true;
			}
		});

		m_tableFilters.setEditorFactory(new IRowEditorFactory<Filter, FilterRowEditor>() {
			@Override
			public @Nonnull
			FilterRowEditor createRowEditor(@Nonnull Filter instance, boolean isnew, boolean isReadonly) throws Exception {
				return new FilterRowEditor(instance, m_tableFilters, cols);
			}
		});

		container.add(m_tableFilters);
	}
}
