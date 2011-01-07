package to.etc.domui.component.graph;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * This is a Small button which shows a selected color, and which opens
 * a color selector to change that color when pressed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ColorPickerButton extends Div implements IHasChangeListener {
	private Input m_hidden = new HiddenInput();

	private Div m_coldiv = new Div();
	private IValueChanged< ? > m_onValueChanged;

	/**
	 * Create the required structure.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-cpbt-btn");
		add(m_hidden);
		add(m_coldiv);
		if(m_hidden.getRawValue() == null)
			m_hidden.setRawValue("ffffff");
		m_coldiv.setBackgroundColor("#" + m_hidden.getRawValue());
		appendCreateJS("WebUI.colorPickerButton('#" + getActualID() + "','#" + m_hidden.getActualID() + "','" + m_hidden.getRawValue() + "'," + Boolean.valueOf(getOnValueChanged() != null) + ");");

		//		appendCreateJS("$('#" + getActualID() + "').ColorPicker({flat: false, color:'" + m_hidden.getRawValue() + "', onChange: function(hsb,hex,rgb) { $('#" + m_hidden.getActualID() + "').val(hex); } });");
	}

	@Override
	public void onAddedToPage(Page p) {
		p.addHeaderContributor(HeaderContributor.loadJavascript("$js/colorpicker.js"), 100);
	}

	public String getValue() {
		return m_hidden.getRawValue();
	}

	public void setValue(String value) {
		if(value == null)
			value = "000000"; // We do not allow null here.
		if(value.startsWith("#"))
			value = value.substring(1); // Remove any #
		m_hidden.setRawValue(value); // Set the color value;
		m_coldiv.setBackgroundColor("#" + m_hidden.getRawValue());
		if(!isBuilt())
			return;

		//-- Force update existing value.
		appendJavascript("$('#" + getActualID() + "').ColorPickerSetColor('" + value + "');");
	}

	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}
}
