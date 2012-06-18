package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;
import javax.jms.IllegalStateException;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.LookupInputBase.ILookupFormModifier;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Component that is based on LookupInput, that allows multiple selection of items by adding found items into selection box.
 * By default selected items are rendered as spans with caption and close button that removes them from selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 13 Oct 2011
 */
public class MultipleLookupInput<T> extends Div implements IControl<List<T>> {


	/**
	 * Specific implementation for use in {@link MultiLookupInput}. It sets inner {@link DataTable} of {@link LookupInput} 
	 * to multi-select mode. 
	 *
	 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
	 * Created on Jun 5, 2012
	 */
	private class MultiLookupInput extends LookupInput<T> {

		private InstanceSelectionModel<T> m_isSelectionModel;

		public MultiLookupInput(Class<T> lookupClass, String[] resultColumns) {
			super(lookupClass, resultColumns);
		}

		@Override
		protected void initSelectionModel() throws Exception {
			m_isSelectionModel = new InstanceSelectionModel<T>(true);
			getDataTable().setSelectionModel(m_isSelectionModel);
			getDataTable().setShowSelection(m_isSelectionModel.isMultiSelect());
			getDataTable().setSelectionAllHandler(new DefaultSelectAllHandler());
			m_isSelectionModel.addListener(new ISelectionListener<T>() {

				@Override
				public void selectionChanged(@Nonnull T row, boolean on) throws Exception {
					showSelectedCount();
				}

				@Override
				public void selectionAllChanged() throws Exception {
					showSelectedCount();
				}
			});
		}

		public Collection<T> getSelectedItems() {
			return m_isSelectionModel.getSelectedSet();
		}

		public void showSelectedCount() throws IllegalStateException {
			final int selectionCount = m_isSelectionModel.getSelectionCount();
			final Window window = getLookupWindow();
			window.setWindowTitle(Msgs.BUNDLE.formatMessage(Msgs.UI_LUI_TTL_MULTI, Integer.valueOf(selectionCount)));
		}

		@Nonnull
		private Window getLookupWindow() throws IllegalStateException {
			LookupForm<T> lookupForm = getLookupForm();
			if(lookupForm != null) {
				return lookupForm.getParent(Window.class);
			}
			throw new IllegalStateException("Lookup form not found where expected");
		}

		@Override
		void handleSetValue(@Nullable T value) throws Exception {
			if(!isPopupShown()) {
				/*
				 * if set from lookup input - business as usual...
				 */
				super.handleSetValue(value);
			} else {
				/*
				 * if set from lookup form, select that value in model, instead od setting value and closing.
				 * Effectively click on row is same as click on check box.
				 */
				if(value != null) {
					m_isSelectionModel.setInstanceSelected(value, !m_isSelectionModel.isSelected(value));
				}
				showSelectedCount();
			}
		}

	}

	private final Map<T, NodeContainer> m_itemNodes = new HashMap<T, NodeContainer>();
	private List<T> m_selectionList = Collections.EMPTY_LIST;
	private boolean m_mandatory;
	private boolean m_disabled;

	private IValueChanged< ? > m_onValueChanged;

	private final MultiLookupInput m_lookupInput;
	private Div m_selectionContainer;
	private INodeContentRenderer<T> m_selectedItemRenderer;
	private String[] m_renderColumns;

	private String m_cssForSelectedItems;

	private String m_cssForSelectionContainer;

	private SmallImgButton m_clearButton;

	/**
	 * This renderer represents default renderer that is used for items in {@link MultipleLookupInput} control.
	 */
	final private INodeContentRenderer<T> DEFAULT_RENDERER = new INodeContentRenderer<T>() {
		@Override
		public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable T object, @Nullable Object parameters) throws Exception {
			if(node == null || !(node instanceof Label)) {
				throw new IllegalArgumentException("Expected Label but found: " + node);
			}
			if(object != null) {
				ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
				if(cmm != null) {
					List<ExpandedDisplayProperty< ? >> xpl = null;
					String[] cols = m_renderColumns;
					if (cols != null && cols.length > 0) {
						xpl = ExpandedDisplayProperty.expandProperties(cmm, cols);
					}else {
						List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
						if (l.size() > 0) {
							xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
						}
					}
					if (xpl != null && xpl.size() > 0) {
						xpl = ExpandedDisplayProperty.flatten(xpl);
						String display = "";
						String hint = "";
						for(ExpandedDisplayProperty< ? > xp : xpl) {
							String val = xp.getPresentationString(object);
							if(val == null || val.length() == 0)
								continue;
							display += (display.length() == 0) ? val : ", " + val;
							hint += (hint.length() == 0) ? xp.getDefaultLabel() : ", " + xp.getDefaultLabel();
						}
						node.setText(display);
						node.setTitle(hint);
					}else {
						node.setText(object.toString());
					}
				}
			}
		}
	};

	private void addSelection() throws Exception {
		for(T item : m_lookupInput.getSelectedItems()) {
			addSelection(item);
		}
		updateClearButtonState();
	}

	public MultipleLookupInput(@Nonnull Class<T> clazz, String... renderColumns) {
		m_lookupInput = new MultiLookupInput(clazz, renderColumns);
		m_lookupInput.setLookupFormInitialization(new ILookupFormModifier<T>() {
			private boolean initialized = false;
			@Override
			public void initialize(@Nonnull LookupForm<T> lf) throws Exception {
				if(!initialized) {
					DefaultButton confirm = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CONFIRM));
					confirm.setIcon("THEME/btnConfirm.png");
					confirm.setTestID("confirmButton");
					confirm.setClicked(new IClicked<NodeBase>() {

						@Override
						public void clicked(NodeBase clickednode) throws Exception {
							addSelection();
							m_lookupInput.closePopup();
						}
					});
					lf.addButtonItem(confirm, 800);
					initialized = true;
				}
			}
		});
		m_renderColumns = renderColumns;
		m_clearButton = new SmallImgButton("THEME/btnClearLookup.png", new IClicked<SmallImgButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(SmallImgButton b) throws Exception {
				clearSelection(null);
			}
		});
		m_clearButton.setTestID("clearButtonInputLookup");
		m_clearButton.setDisplay(DisplayType.NONE);

	}

	protected void clearSelection(Object object) throws Exception {
		m_selectionList.clear();
		m_itemNodes.clear();
		m_selectionContainer.removeAllChildren();
		updateClearButtonState();
		IValueChanged<MultipleLookupInput< ? >> ovc = (IValueChanged<MultipleLookupInput< ? >>) getOnValueChanged();
		if(ovc != null) {
			ovc.onValueChanged(this);
		}
	}

	private void addClearButton() throws Exception {
		if(!m_lookupInput.isBuilt()) {
			m_lookupInput.build();
		}

		//m_lookupInput is frequently rebuilding, from this reason we need to 'insert' out button inside after every rebuild of m_lookupInput
		List<SmallImgButton> btns = m_lookupInput.getDeepChildren(SmallImgButton.class);
		if(btns.size() > 0) {
			//we append custom clear button right after last button in inner lookup input
			btns.get(btns.size() - 1).appendAfterMe(m_clearButton);
		} else {
			//if there are no buttons, then just append lookup input
			m_lookupInput.appendAfterMe(m_clearButton);
		}
		updateClearButtonState();
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(m_lookupInput);
		m_lookupInput.setOnValueChanged(new IValueChanged<LookupInput<T>>() {
			@Override
			public void onValueChanged(@Nonnull LookupInput<T> component) throws Exception {
				T item = component.getValueSafe();
				if(item != null) {
					addSelection(item);
					component.setValue(null);
					addClearButton();
					IValueChanged<MultipleLookupInput< ? >> ovc = (IValueChanged<MultipleLookupInput< ? >>) getOnValueChanged();
					if(ovc != null) {
						ovc.onValueChanged(MultipleLookupInput.this);
					}
				}
			}
		});
		renderSelection();
		addClearButton();
	}

	public void addSelection(T item) throws Exception {
		if(m_selectionList == Collections.EMPTY_LIST) {
			m_selectionList = new ArrayList<T>();
		}
		if(!m_selectionList.contains(item)) {
			m_selectionList.add(item);
			final Span itemNode = createItemNode(item);
			m_selectionContainer.add(itemNode);
			m_itemNodes.put(item, itemNode);
			updateClearButtonState();
		}
	}

	private void renderSelection() throws Exception {
		m_selectionContainer = new Div();
		if(getCssForSelectionContainer() != null) {
			m_selectionContainer.setCssClass(getCssForSelectionContainer());
		} else {
			m_selectionContainer.setCssClass("ui-mli-cnt");
		}
		for (final T item : m_selectionList) {
			final Span itemNode = createItemNode(item);
			m_selectionContainer.add(itemNode);
		}
		add(m_selectionContainer);
	}

	private Span createItemNode(final T item) throws Exception {
		final Span itemNode = new Span();
		Label itemText = new Label();
		if(getCssForSelectedItems() != null) {
			itemNode.setCssClass(getCssForSelectedItems());
		} else {
			itemNode.setCssClass("ui-mli-itm");
		}
		Img imgClose = new Img("THEME/btnDelete.png");
		imgClose.setMarginLeft("2px");
		itemNode.add(itemText);
		itemNode.add(imgClose);

		final IClicked<NodeBase> removeHandler = new IClicked<NodeBase>() {

			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				removeItem(item);
			}

		};
		imgClose.setClicked(removeHandler);


		//In case of rendring selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
		INodeContentRenderer<T> r = getSelectedItemContentRenderer();
		if(r == null)
			r = DEFAULT_RENDERER; // Prevent idiotic generics error
		r.renderNodeContent(this, itemText, item, null);
		return itemNode;
	}

	@Nonnull
	public LookupInput<T> getMultipleLookupInput() {
		return m_lookupInput;
	}

	protected void updateClearButtonState() {
		m_clearButton.setDisplay(m_selectionList.size() == 0 ? DisplayType.NONE : DisplayType.INLINE);
	}

	public LookupInput<T> getLookupInput() {
		return m_lookupInput;
	}

	@Override
	public List<T> getValueSafe() {
		return m_selectionList;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public boolean isReadOnly() {
		return m_disabled;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if (m_disabled != ro) {
			m_disabled = ro;
			if (isBuilt()) {
				forceRebuild();
			}
		}
	}

	@Override
	public List<T> getValue() {
		return m_selectionList;
	}

	@Override
	public void setValue(@Nullable List<T> v) {
		if (m_selectionList != v) {
			m_selectionList = v;
			if (isBuilt()) {
				forceRebuild();
			}
		}
	}

	@Override
	public void setDisabled(boolean d) {
		setReadOnly(d);
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	@Nullable
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	public @Nonnull IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}

	public INodeContentRenderer<T> getSelectedItemContentRenderer() {
		return m_selectedItemRenderer;
	}

	public void setSelectedItemContentRenderer(INodeContentRenderer<T> render) {
		if(m_selectedItemRenderer != render) {
			m_selectedItemRenderer = render;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public String[] getRenderColumns() {
		return m_renderColumns;
	}

	public void setRenderColumns(String[] renderColumns) {
		if(m_renderColumns != renderColumns) {
			m_renderColumns = renderColumns;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public String getCssForSelectedItems() {
		return m_cssForSelectedItems;
	}

	public void setCssForSelectedItems(String cssForSelectedItems) {
		if(m_cssForSelectedItems != cssForSelectedItems) {
			m_cssForSelectedItems = cssForSelectedItems;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public String getCssForSelectionContainer() {
		return m_cssForSelectionContainer;
	}

	public void setCssForSelectionContainer(String cssForSelectionContainer) {
		m_cssForSelectionContainer = cssForSelectionContainer;
	}

	/**
	 * Remove item from selection list.
	 * @param item
	 * @throws Exception
	 */
	public void removeItem(final T item) throws Exception {
		NodeBase itemNode = m_itemNodes.get(item);
		itemNode.remove();
		m_selectionList.remove(item);
		if(getOnValueChanged() != null) {
			//FIXME: from some reason we can't pass items here -> some buggy generics issue is shown if we specifiy item as argumen!?
			//getOnValueChanged().onValueChanged(item);
			((IValueChanged<MultipleLookupInput<T>>) getOnValueChanged()).onValueChanged(MultipleLookupInput.this);
		}
		updateClearButtonState();
	}

	public void setSearchImmediately(boolean searchImmediately) {
		m_lookupInput.setSearchImmediately(searchImmediately);
	}
}
