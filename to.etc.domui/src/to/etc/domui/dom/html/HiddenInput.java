package to.etc.domui.dom.html;

/**
 * A hidden input field. Internal use to help with communicating with input components.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class HiddenInput extends Input {
	public HiddenInput() {
		setSpecialAttribute("s", "true");
	}

	@Override
	public String getInputType() {
		return "hidden";
	}
}
