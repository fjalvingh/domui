package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class DropDownPicker<T> extends SmallImgButton {

	public interface IDropDownPickerAdjuster<T> {
		void onBeforeShow(ComboLookup<T> m_picker) throws Exception;
	}

	private List<T> m_data;

	private IValueSelected<T> m_onValueSelected;

	private ComboLookup<T> m_picker;

	private NodeBase m_zIndexBaseParent;

	private final int m_size;

	private int m_offsetX = 0;

	private int m_offsetY = 0;

	private T m_selected;

	private IDropDownPickerAdjuster<T> m_adjuster;

	/**
	 * DropDownPicker constructor. By default size of drop down list is 8.
	 * @param zIndexBaseParent pass node that has zIndex that would be used as base for showing popup (z index of popup is increased for 10)
	 * @param data data for picker popup
	 */
	public DropDownPicker(@Nonnull NodeBase zIndexBaseParent, @Nonnull List<T> data) {
		this(zIndexBaseParent, data, 8);
	}

	/**
	 * DropDownPicker constructor.
	 * @param zIndexBaseParent pass node that has zIndex that would be used as base for showing popup (z index of popup is increased for 10)
	 * @param data data for picker popup
	 * @param size size of drop down list
	 */
	public DropDownPicker(@Nonnull NodeBase zIndexBaseParent, @Nonnull List<T> data, int size) {
		m_data = data;
		m_zIndexBaseParent = zIndexBaseParent;
		m_size = size;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		if(getSrc() == null) {
			setSrc(Msgs.BTN_FIND);
		}
		m_picker = new ComboLookup<T>(m_data);
		m_picker.setDisplay(DisplayType.NONE);
		m_picker.setPosition(PositionType.ABSOLUTE);
		if(m_zIndexBaseParent.getZIndex() > Integer.MIN_VALUE) {
			m_picker.setZIndex(m_zIndexBaseParent.getZIndex() + 10);
		} else {
			m_picker.setZIndex(10);
		}
		m_picker.setSize(m_size);
		m_picker.setOnValueChanged(new IValueChanged<NodeBase>() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void onValueChanged(NodeBase component) throws Exception {
				m_picker.setDisplay(DisplayType.BLOCK);//we need to change twice since value is changed using javascript
				m_picker.setDisplay(DisplayType.NONE);
				if(getOnValueSelected() != null) {
					m_selected = m_picker.getValue();
					getOnValueSelected().valueSelected(m_selected);
				}
			}
		});
		if(m_selected != null) {
			m_picker.setValue(m_selected);
			m_picker.setMandatory(true);
		} else if(m_data.size() > 0) {
			m_picker.setValue(m_data.get(0));
			m_picker.setMandatory(true);
		}
		m_picker.setSpecialAttribute("onblur", "this.style.display='none';");

		if(getClicked() == null) {
			setClicked(new IClicked<SmallImgButton>() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void clicked(SmallImgButton clickednode) throws Exception {
					if(getAdjuster() != null) {
						getAdjuster().onBeforeShow(m_picker);
					}

					appendJavascript("$('#" + m_picker.getActualID() + "').css('top', $('#" + getActualID() + "').position().top + " + m_offsetY + " + $('#" + getActualID() + "').outerHeight() - 1);");
					appendJavascript("$('#" + m_picker.getActualID() + "').css('left', $('#" + getActualID() + "').position().left + " + m_offsetX + " - $('#" + m_picker.getActualID() + "').outerWidth() + $('#"
						+ getActualID() + "').outerWidth() - 3);");
					appendJavascript("$('#" + m_picker.getActualID() + "').css('display', 'inline');");
					appendJavascript("$('#" + m_picker.getActualID() + "').focus();");
					if(m_picker.getSelectedIndex() >= 0) {
						appendJavascript("WebUI.makeOptionVisible('" + m_picker.getOption(m_picker.getSelectedIndex()).getActualID() + "');");
					}
				}
			});
		}

		appendAfterMe(m_picker);
	}

	public IValueSelected<T> getOnValueSelected() {
		return m_onValueSelected;
	}

	/**
	 * Register listener for on value selected event.
	 * @param onValueSelected
	 */
	public void setOnValueSelected(IValueSelected<T> onValueSelected) {
		m_onValueSelected = onValueSelected;
	}

	/**
	 * Returns size of drop down list.
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	/**
	 * Returns custom offset x relative to picker btn.
	 * @return
	 */
	public int getOffsetX() {
		return m_offsetX;
	}

	/**
	 * Specify custom offset x relative to picker btn. By default, popup is rendered under picker button.
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
	 * Returns custom offset y relative to picker btn.
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

	public void setValue(T value) {
		m_selected = value;
		if(m_picker != null) {
			m_picker.setValue(value);
		}
	}

	public IDropDownPickerAdjuster<T> getAdjuster() {
		return m_adjuster;
	}

	public void setAdjuster(IDropDownPickerAdjuster<T> adjuster) {
		m_adjuster = adjuster;
	}
}
