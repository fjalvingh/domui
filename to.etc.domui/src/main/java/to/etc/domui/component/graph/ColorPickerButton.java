package to.etc.domui.component.graph;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HiddenInput;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IHasChangeListener;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;

/**
 * This is a Small button which shows a selected color, and which opens
 * a color selector to change that color when pressed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ColorPickerButton extends Div implements IHasChangeListener, IControl<String> {
	private Input m_hidden = new HiddenInput();

	private Div m_coldiv = new Div();
	private IValueChanged< ? > m_onValueChanged;

	private boolean m_mandatory;

	private boolean m_disabled;

	private boolean m_readOnly;

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
		if(isOff()) {
			//-- Do not attach the picker at all: the button then just shows the color.
			addCssClass("ui-cpbt-off");
			return;
		}
		appendCreateJS("WebUI.colorPickerButton('#" + getActualID() + "','#" + m_hidden.getActualID() + "','" + m_hidden.getRawValue() + "'," + Boolean.valueOf(getOnValueChanged() != null) + ");");

		//		appendCreateJS("$('#" + getActualID() + "').ColorPicker({flat: false, color:'" + m_hidden.getRawValue() + "', onChange: function(hsb,hex,rgb) { $('#" + m_hidden.getActualID() + "').val(hex); } });");
	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Override
	public void onAddedToPage(Page p) {
		p.addHeaderContributor(HeaderContributor.loadJavascript("$js/colorpicker.js"), 100);
	}

	public static void initialize(UrlPage page) {
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/colorpicker.js"), 100);
	}

	@Override
	public String getValue() {
		return m_hidden.getRawValue();
	}

	@Override
	public String getValueSafe() {
		return getValue();
	}

	@Override
	public void setValue(@Nullable String value) {
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

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/**
	 * T when the button must not open the picker, for whatever reason.
	 */
	private boolean isOff() {
		return m_disabled || m_readOnly;
	}

	@Override
	public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		m_disabled = d;
		forceRebuild();
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if(m_readOnly == ro)
			return;
		m_readOnly = ro;
		forceRebuild();
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
	public void setMandatory(boolean ro) {
		m_mandatory = ro;
	}

	@Override public void setHint(String hintText) {
		setTitle(hintText);
	}
}
