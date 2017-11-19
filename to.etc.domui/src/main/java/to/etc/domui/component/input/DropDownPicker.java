package to.etc.domui.component.input;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.event.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * Control that behaves as {@link SmallImgButton} that has built in click handler that popups select list with predefined data to choose from.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 26, 2012
 */
public class DropDownPicker<T> extends SmallImgButton implements IControl<T> {
	public static final int DEFAULT_COMBO_SIZE = 8;

	public enum HAlign {LEFT, MIDDLE, RIGHT}

	@Nullable
	private IValueChanged<DropDownPicker<T>> m_onValueChanged;

	@Nullable
	private List<T> m_data;

	@Nonnull
	private final ComboLookup<T> m_picker;

	private int m_size = DEFAULT_COMBO_SIZE;

	private int m_offsetX = 0;

	private int m_offsetY = 0;

	@Nullable
	private T m_selected;

	private boolean m_mandatory = true;

	@Nullable
	private INotifyEvent<DropDownPicker<T>, ComboLookup<T>> m_onBeforeShow;

	@Nonnull
	private HAlign m_halign = HAlign.LEFT;

	/**
	 * Control that drop down picker would use as base for vertical and horizontal alignment. Picker is always shown below base control, while horizontal alignment can be defined (see {@link DropDownPicker#setHalign(HAlign)}).
	 * If not set different, picker {@link SmallImgButton} is used as alignment base.
	 */
	@Nullable
	private NodeBase m_alignmentBase;

	/**
	 * DropDownPicker constructor. By default size of drop down list is DropDownPicker.DEFAULT_COMBO_SIZE.
	 */
	public DropDownPicker() {
		m_picker = new ComboLookup<T>();
	}

	/**
	 * DropDownPicker constructor. By default size of drop down list is DropDownPicker.DEFAULT_COMBO_SIZE.
	 * @param data data for picker popup
	 */
	public DropDownPicker(@Nonnull List<T> data) {
		this(data, DEFAULT_COMBO_SIZE);
	}

	/**
	 * DropDownPicker constructor.
	 * @param data data for picker popup
	 * @param size size of drop down list
	 */
	public DropDownPicker(@Nonnull List<T> data, int size) {
		m_data = data;
		m_size = size;
		m_picker = new ComboLookup<T>(m_data);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		if(getSrc() == null) {
			setSrc(Msgs.BTN_FIND);
		}
		m_picker.setDisplay(DisplayType.NONE);
		m_picker.setPosition(PositionType.ABSOLUTE);

		//we use this to calculate correct zIndex in drop down picker later
		NodeBase zIndexNode = getParentOfTypes(Window.class, UrlPage.class);

		if(zIndexNode == null) {
			zIndexNode = getParent();
		}

		if(zIndexNode.getZIndex() > Integer.MIN_VALUE) {
			m_picker.setZIndex(zIndexNode.getZIndex() + 10);
		} else {
			m_picker.setZIndex(10);
		}
		m_picker.setSize(m_size);
		m_picker.setHeight("auto");
		m_picker.setClicked(new IClicked<NodeBase>() {

			@Override
			public void clicked(@Nonnull NodeBase node) throws Exception {
				handlePickerValueChanged();
			}
		});
		m_picker.setReturnPressed(new IReturnPressed<Select>() {

			@Override
			public void returnPressed(@Nonnull Select node) throws Exception {
				handlePickerValueChanged();
			}
		});

		List<T> data = m_data;
		if(m_selected != null) {
			m_picker.setValue(m_selected);
		} else if(data != null && data.size() > 0 && isMandatory()) {
			m_picker.setValue(data.get(0));
		}

		m_picker.setMandatory(isMandatory());

		m_picker.setSpecialAttribute("onblur", "$(this).css('display','none');$(this).triggerHandler('click')");

		if(getClicked() == null) {
			setClicked(m_defaultClickHandler);
		}

		appendAfterMe(m_picker);
		positionPicker();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_picker.getForTarget();
	}

	void handlePickerValueChanged() throws Exception {
		appendJavascript("$('#" + m_picker.getActualID() + "').css('display', 'none');");
		m_selected = m_picker.getValue();
		IValueChanged<DropDownPicker<T>> onValueChanged = getOnValueChanged();
		if(onValueChanged != null) {
			onValueChanged.onValueChanged(this);
		}
	}

	private final @Nonnull
	IClicked<SmallImgButton> m_defaultClickHandler = new IClicked<SmallImgButton>() {
		@SuppressWarnings("synthetic-access")
		@Override
		public void clicked(@Nonnull SmallImgButton clickednode) throws Exception {
			INotifyEvent<DropDownPicker<T>, ComboLookup<T>> onBeforeShow = getOnBeforeShow();
			if(onBeforeShow != null) {
				onBeforeShow.onNotify(DropDownPicker.this, m_picker);
			}
			positionPicker();
			appendJavascript("$('#" + m_picker.getActualID() + "').css('display', 'inline');");
			appendJavascript("$('#" + m_picker.getActualID() + "').focus();");
			if(m_picker.getSelectedIndex() >= 0) {
				appendJavascript("WebUI.makeOptionVisible('" + m_picker.getOption(m_picker.getSelectedIndex()).getActualID() + "');");
			}
		}
	};

	private void positionPicker() {
		NodeBase alignBase = getAlignmentBase();
		if(alignBase == null) {
			alignBase = DropDownPicker.this;
		}

		m_picker.alignTopToBottom(alignBase, m_offsetY, false);
		switch(m_halign){
			case LEFT:
				m_picker.alignToLeft(alignBase, m_offsetX, false);
				break;
			case RIGHT:
				m_picker.alignToRight(alignBase, m_offsetX, false);
				break;
			case MIDDLE:
				m_picker.alignToMiddle(alignBase, m_offsetX, false);
				break;
			default:
				throw new IllegalStateException("Unknown horizontal alignment? Found : " + m_halign);
		}
	}

	/**
	 * Returns size of drop down list.
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		m_size = size;
		if(m_picker != null) {
			m_picker.setSize(m_size);
		}
	}

	/**
	 * Returns custom offset x relative to picker btn.
	 * @return
	 */
	public int getOffsetX() {
		return m_offsetX;
	}

	/**
	 * Specify custom offset x relative to picker btn. By default, popup is rendered under {@link DropDownPicker#getAlignmentBase()} control.
	 * @param offsetX
	 */
	public void setOffsetX(int offsetX) {
		if(m_offsetX != offsetX) {
			m_offsetX = offsetX;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	/**
	 * Returns custom offset y relative to {@link DropDownPicker#getAlignmentBase()} control. To set horizontal alignment rule see {@link DropDownPicker#setHalign(HAlign)}.
	 * @return
	 */
	public int getOffsetY() {
		return m_offsetY;
	}

	/**
	 * Specify custom offset y relative to picker btn. By default, popup is rendered under picker button.
	 * @param offsetX
	 */
	public void setOffsetY(int offsetY) {
		if(m_offsetY != offsetY) {
			m_offsetY = offsetY;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	@Override
	public void setValue(@Nullable T value) {
		m_selected = value;
		if(m_picker != null) {
			m_picker.setValue(value);
		}
	}

	@Override
	public @Nullable
	T getValue() {
		return m_selected;
	}

	public @Nullable
	INotifyEvent<DropDownPicker<T>, ComboLookup<T>> getOnBeforeShow() {
		return m_onBeforeShow;
	}

	public void setOnBeforeShow(@Nullable INotifyEvent<DropDownPicker<T>, ComboLookup<T>> onBeforeShow) {
		m_onBeforeShow = onBeforeShow;
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		if(m_mandatory == mandatory)
			return;
		m_mandatory = mandatory;
		if(isBuilt()) {
			forceRebuild();
		}
	}

	/**
	 * Horizontal alignment of dropdown popup. By default set to {@link HAlign#Left}.
	 * @return
	 */
	@Nonnull
	public HAlign getHalign() {
		return m_halign;
	}

	/**
	 * Sets picker select list horizontal alignment.
	 * <UL>
	 * <LI> {@link HAlign#LEFT} position list to be left aligned with {@link DropDownPicker#getAlignmentBase()} component.</LI>
	 * <LI> {@link HAlign#MIDDLE} position list to be center aligned with {@link DropDownPicker#getAlignmentBase()} component.</LI>
	 * <LI> {@link HAlign#RIGHT} position list to be right aligned with {@link DropDownPicker#getAlignmentBase()} component.</LI>
	 * </UL>
	 *
	 * If no other component is set to be {@link DropDownPicker#getAlignmentBase()}, this defaults to drop down button.
	 * In order to affect offset to this aligment rules use {@link DropDownPicker#setOffsetY(int)} and {@link DropDownPicker#setOffsetX(int)}.
	 * @param halign
	 */
	public void setHalign(@Nonnull HAlign halign) {
		m_halign = halign;
		if(isBuilt()) {
			positionPicker();
		}
	}

	/**
	 * see {@link DropDownPicker#m_alignmentBase}
	 * @param halignmentBase
	 */
	public @Nullable
	NodeBase getAlignmentBase() {
		return m_alignmentBase;
	}

	/**
	 * see {@link DropDownPicker#m_alignmentBase}
	 * @param halignmentBase
	 */
	public void setAlignmentBase(@Nullable NodeBase halignmentBase) {
		m_alignmentBase = halignmentBase;
	}

	public @Nonnull
	Select getSelectControl() throws Exception {
		return m_picker;
	}

	@Nonnull
	private List<T> getData() {
		List<T> data = m_data;
		if(null == data)
			throw new IllegalStateException("Data is null");
		return data;
	}

	public boolean hasData() {
		return m_data != null;
	}

	public void setData(@Nonnull List<T> data) {
		if(m_data != data) {
			m_data = data;
			m_picker.setData(data);
			if(m_selected != null && !data.contains(m_selected)) {
				m_selected = null;
				m_picker.setValue(null);
			}
		}
	}

	@Override
	@Nullable
	public IValueChanged<DropDownPicker<T>> getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(@Nullable IValueChanged< ? > onValueChanged) {
		m_onValueChanged = (IValueChanged<DropDownPicker<T>>) onValueChanged;
	}

	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setReadOnly(boolean ro) {
		throw new UnsupportedOperationException("setReadOnly() not supported in " + getClass().getName());
	}
}
