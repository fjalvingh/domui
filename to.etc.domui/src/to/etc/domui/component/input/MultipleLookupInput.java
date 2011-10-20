package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
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
public class MultipleLookupInput<T> extends Div implements IInputNode<List<T>> {
	private List<T> m_selectionList = Collections.EMPTY_LIST;
	private boolean m_mandatory;
	private boolean m_disabled;

	private IValueChanged< ? > m_onValueChanged;
	private final LookupInput<T> m_lookupInput;
	private Div m_selectionContainer;
	private INodeContentRenderer<T> m_selectedItemRenderer;
	private String[] m_renderColumns;

	private String m_cssForSelectedItems;

	private String m_cssForSelectionContainer;

	private SmallImgButton m_clearButton;

	/**
	 * This renderer represents default renderer that is used for items in {@link MultipleLookupInput} control.
	 */
	private INodeContentRenderer<T> DEAFULT_RENDERER = new INodeContentRenderer<T>() {
		@Override
		public void renderNodeContent(NodeBase component, NodeContainer node, T object, Object parameters) throws Exception {
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

	public MultipleLookupInput(@Nonnull LookupInput<T> lookupInput, String... renderColumns) {
		m_lookupInput = lookupInput;
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
		m_selectionContainer.removeAllChildren();
		updateClearButtonState();
		if(getOnValueChanged() != null) {
			getOnValueChanged().onValueChanged(null);
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
			public void onValueChanged(LookupInput<T> component) throws Exception {
				T item = component.getValueSafe();
				if(item != null) {
					addSelection(item);
					component.setValue(null);
					addClearButton();
					if(getOnValueChanged() != null) {
						getOnValueChanged().onValueChanged(null);
					}
				}
			}
		});
		renderSelection();
		addClearButton();
	}

	protected void addSelection(T item) throws Exception {
		if(m_selectionList == Collections.EMPTY_LIST) {
			m_selectionList = new ArrayList<T>();
		}
		if(!m_selectionList.contains(item)) {
			m_selectionList.add(item);
			m_selectionContainer.add(createItemNode(item));
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
		imgClose.setClicked(new IClicked<NodeBase>() {

			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				itemNode.remove();
				m_selectionList.remove(item);
				if(getOnValueChanged() != null) {
					//FIXME: from some reason we can't pass items here -> some buggy generics issue is shown if we specifiy item as argumen!?
					//getOnValueChanged().onValueChanged(item);
					getOnValueChanged().onValueChanged(null);
				}
				updateClearButtonState();
			}

		});

		//In case of rendring selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
		INodeContentRenderer<T> r = getSelectedItemContentRenderer();
		if(r == null)
			r = DEAFULT_RENDERER; // Prevent idiotic generics error
		r.renderNodeContent(this, itemText, item, null);
		return itemNode;
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
	public void setValue(List<T> v) {
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

	@Override
	public IBinder bind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
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
}
