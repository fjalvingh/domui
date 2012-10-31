package to.etc.domui.log;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.log.data.*;
import to.etc.domui.util.*;
import to.etc.log.*;
import to.etc.log.handler.*;
import to.etc.webapp.nls.*;

public class HandlerRowEditor extends Div implements IEditor {
	protected static final BundleRef BUNDLE = Msgs.BUNDLE;

	private final Handler m_instance;

	private final TableModelTableBase<Handler> m_model;

	private HorizontalFormBuilder m_builder;

	private ModelBindings m_bindings;

	private ExpandingEditTable<Matcher> m_tableMatchers;

	private SimpleListModel<Matcher> m_modelMatchers;

	private ExpandingEditTable<Filter> m_tableFilters;

	private SimpleListModel<Filter> m_modelFilters;

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
		m_builder = new HorizontalFormBuilder(m_instance);
		final IControl<HandlerType> typeCtl = (IControl<HandlerType>) m_builder.addProp(Handler.pTYPE);
		final IControl<String> nameCtl = (IControl<String>) m_builder.addProp(Handler.pFILE);
		container.add(m_builder.finish());
		m_bindings = m_builder.getBindings();
		m_bindings.moveModelToControl();
		final TextNode holder = new TextNode(nameCtl.getValueSafe());
		typeCtl.setOnValueChanged(new IValueChanged<NodeBase>() {
			@Override
			public void onValueChanged(@Nonnull NodeBase component) throws Exception {
				updateNameByType(typeCtl, nameCtl, holder);
			}
		});
		if(HandlerType.STDOUT == typeCtl.getValue()) {
			updateNameByType(typeCtl, nameCtl, holder);
		}
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
			public void clicked(LinkButton clickednode) throws Exception {
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
			public void clicked(LinkButton clickednode) throws Exception {
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
