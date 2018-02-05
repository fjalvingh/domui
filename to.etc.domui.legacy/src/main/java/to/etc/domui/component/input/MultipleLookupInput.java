package to.etc.domui.component.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.HoverButton;
import to.etc.domui.component.input.LookupInputBase.ILookupFormModifier;
import to.etc.domui.component.input.LookupInputBase.IPopupOpener;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.DefaultSelectAllHandler;
import to.etc.domui.component.tbl.ISelectionListener;
import to.etc.domui.component.tbl.InstanceSelectionModel;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.themes.Theme;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Component that is based on LookupInput, that allows multiple selection of items by adding found items into selection box.
 * By default selected items are rendered as spans with caption and close button that removes them from selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 13 Oct 2011
 */
public class MultipleLookupInput<T> extends Div implements IControl<List<T>>, ITypedControl<T> {
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
			return m_isSelectionModel != null ? m_isSelectionModel.getSelectedSet() : Collections.EMPTY_SET;
		}

		public void showSelectedCount() throws IllegalStateException {
			final int selectionCount = m_isSelectionModel.getSelectionCount();
			final Window window = getLookupWindow();
			window.setWindowTitle(Msgs.BUNDLE.formatMessage(Msgs.UI_LUI_TTL_MULTI, Integer.valueOf(selectionCount)));
		}

		@Nonnull
		private Window getLookupWindow() throws IllegalStateException {
			SearchPanel<T> lookupForm = getSearchPanel();
			if(lookupForm != null) {
				return lookupForm.getParent(Window.class);
			}
			throw new IllegalStateException("Lookup form not found where expected");
		}

		@Override
		protected void handleSetValue(@Nullable T value) throws Exception {
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
	private IRenderInto<T> m_selectedItemRenderer;
	private String[] m_renderColumns;

	private String m_cssForSelectedItems;

	private String m_cssForSelectionContainer;

	private String m_maxHeightForSelectionContainer;

	private HoverButton m_clearButton;

	private Stack<Integer> m_updateStack = new Stack<Integer>();

	/**
	 * This renderer represents default renderer that is used for items in {@link MultipleLookupInput} control.
	 */
	final private IRenderInto<T> DEFAULT_RENDERER = new IRenderInto<T>() {
		@Override
		public void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
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

	public MultipleLookupInput(@Nonnull Class<T> clazz, String... renderColumns) {
		m_lookupInput = new MultiLookupInput(clazz, renderColumns);
		m_lookupInput.setSearchPanelInitialization(new ILookupFormModifier<T>() {
			private boolean initialized = false;
			@Override
			public void initialize(@Nonnull SearchPanel<T> lf) throws Exception {
				if(!initialized) {
					DefaultButton confirm = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CONFIRM));
					confirm.setIcon("THEME/btnConfirm.png");
					confirm.setTestID("confirmButton");
					confirm.setClicked(new IClicked<NodeBase>() {

						@Override
						public void clicked(@Nonnull NodeBase clickednode) throws Exception {
							m_lookupInput.closePopup();
							addSelection();
						}
					});
					lf.addButtonItem(confirm, 800);
					initialized = true;
				}
			}
		});
		m_renderColumns = renderColumns;
		m_clearButton = new HoverButton(Theme.BTN_HOVERMULTILOOKUKPCLEAR, new IClicked<HoverButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(@Nonnull HoverButton b) throws Exception {
				clearSelection(null);
			}
		});
		m_clearButton.setTestID("clearButtonInputLookup");
		m_clearButton.setDisplay(DisplayType.NONE);
		m_clearButton.addCssClass("ui-lui-clear-mul-btn");

	}

	private void addSelection() throws Exception {
		startUpdate();
		for(T item : m_lookupInput.getSelectedItems()) {
			addSelection(item);
		}
		endUpdate();
	}

	@Nonnull @Override public Class<T> getActualType() {
		return m_lookupInput.getActualType();
	}

	protected void clearSelection(Object object) throws Exception {
		startUpdate();
		m_selectionList.clear();
		m_itemNodes.clear();
		m_selectionContainer.removeAllChildren();
		applyIE10Workaround();
		endUpdate();
	}

	@Nullable @Override protected String getFocusID() {
		return m_lookupInput.getFocusID();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_lookupInput.getForTarget();
	}

	/**
	 * There is a problem in Internet Explorer 10 with min and max height parameters for empty div.<br/>
	 * The problem occurs when container have scroller (bigger than max height) and than it's cleared.
	 */
	protected void applyIE10Workaround() {
		Span dummySpan = new Span();
		m_selectionContainer.add(dummySpan);
	}

	private void addClearButton() throws Exception {
		if(!m_lookupInput.isBuilt()) {
			m_lookupInput.build();
		}

		//m_lookupInput is frequently rebuilding, from this reason we need to 'insert' out button inside after every rebuild of m_lookupInput
		List<HoverButton> btns = m_lookupInput.getDeepChildren(HoverButton.class);
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
					DomUtil.setModifiedFlag(component);
					addSelection(item);
					component.setValue(null);
					addClearButton();
					IValueChanged<MultipleLookupInput< ? >> ovc = (IValueChanged<MultipleLookupInput< ? >>) getOnValueChanged();
					if(ovc != null) {
						ovc.onValueChanged(MultipleLookupInput.this);
					}
					component.setFocus();
				}
			}
		});
		renderSelection();
		addClearButton();
		if ((isDisabled() || isReadOnly()) && !getValueSafe().isEmpty()){
			m_lookupInput.setDisplay(DisplayType.NONE);
		}
	}

	public void addSelection(T item) throws Exception {
		if(m_selectionList == Collections.EMPTY_LIST) {
			m_selectionList = new ArrayList<T>();
		}
		if(!m_selectionList.contains(item)) {
			startUpdate();
			m_selectionList.add(item);
			final Span itemNode = createItemNode(item);
			m_selectionContainer.add(itemNode);
			m_itemNodes.put(item, itemNode);
			endUpdate();
		}
	}

	/**
	 * Call once you want to mark start of selection update. Follow with endUpdate once whole change is done in order to trigger UI needed changes.
	 */
	public void startUpdate() {
		m_updateStack.push(Integer.valueOf(m_selectionList.size()));
	}

	/**
	 * Call to mark end selection update changes. See also startUpdate.
	 * @throws Exception
	 */
	public void endUpdate() throws Exception {
		if(m_updateStack.isEmpty()) {
			throw new IllegalStateException("Update stack can not be empty!");
		}
		Integer val = m_updateStack.pop();
		if(m_updateStack.isEmpty()) {
			DomUtil.setModifiedFlag(this);
			updateClearButtonState();
			int currentSize = m_selectionList.size();
			if(currentSize != val.intValue()) {
				IValueChanged<MultipleLookupInput< ? >> ovc = (IValueChanged<MultipleLookupInput< ? >>) getOnValueChanged();
				if(ovc != null) {
					ovc.onValueChanged(MultipleLookupInput.this);
				}
			}
		}
	}

	private void renderSelection() throws Exception {
		m_selectionContainer = new Div();
		if(getCssForSelectionContainer() != null) {
			m_selectionContainer.setCssClass(getCssForSelectionContainer());
		} else {
			m_selectionContainer.setCssClass("ui-mli-cnt");
		}
		String maxHeightForSelectionContainer = getMaxHeightForSelectionContainer();
		if(null != maxHeightForSelectionContainer) {
			m_selectionContainer.setMaxHeight(maxHeightForSelectionContainer);
		}
		for (final T item : m_selectionList) {
			final Span itemNode = createItemNode(item);
			m_itemNodes.put(item, itemNode);
			m_selectionContainer.add(itemNode);
		}
		add(m_selectionContainer);
	}

	private Span createItemNode(@Nonnull final T item) throws Exception {
		final Span itemNode = new Span();
		Label itemText = new Label();
		if(getCssForSelectedItems() != null) {
			itemNode.setCssClass(getCssForSelectedItems());
		} else {
			itemNode.setCssClass("ui-mli-itm");
		}
		Img imgClose = new Img(Theme.BTN_CLOSE);
		itemNode.add(itemText);
		itemNode.add(imgClose);

		final IClicked<NodeBase> removeHandler = new IClicked<NodeBase>() {

			@Override
			public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				removeItem(item);
			}

		};
		imgClose.setClicked(removeHandler);


		//In case of rendring selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
		IRenderInto<T> r = getSelectedItemContentRenderer();
		if(r == null)
			r = DEFAULT_RENDERER; // Prevent idiotic generics error
		r.render(itemText, item);
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

	public IRenderInto<T> getSelectedItemContentRenderer() {
		return m_selectedItemRenderer;
	}

	public void setSelectedItemContentRenderer(IRenderInto<T> render) {
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

	public String getMaxHeightForSelectionContainer() {
		return m_maxHeightForSelectionContainer;
	}

	public void setMaxHeightForSelectionContainer(String height) {
		m_maxHeightForSelectionContainer = height;
	}

	/**
	 * Remove item from selection list.
	 * @param item
	 * @throws Exception
	 */
	public void removeItem(final T item) throws Exception {
		startUpdate();
		NodeBase itemNode = m_itemNodes.get(item);
		itemNode.remove();
		m_selectionList.remove(item);
		endUpdate();
	}

	public void setSearchImmediately(boolean searchImmediately) {
		m_lookupInput.setSearchImmediately(searchImmediately);
	}

	@Nullable
	public List<T> getBindValue() {
		List<T> val = getValue();
		if((val == null || val.isEmpty()) && isMandatory()) {
			throw new ValidationException(Msgs.MANDATORY);
		}
		return val;
	}

	public void setBindValue(@Nullable List<T> value) {
		setValue(value);
	}

	@Nullable
	public LookupInputBase.IPopupOpener getPopupOpener() {
		return m_lookupInput.getPopupOpener();
	}

	public void setPopupOpener(@Nullable IPopupOpener popupOpener) {
		m_lookupInput.setPopupOpener(popupOpener);
	}

	@Nullable
	public IActionAllowed getIsLookupAllowed() {
		return m_lookupInput.getIsLookupAllowed();
	}

	public void setIsLookupAllowed(@Nullable IActionAllowed isLookupAllowed) {
		m_lookupInput.setIsLookupAllowed(isLookupAllowed);
	}
}
