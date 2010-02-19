package to.etc.domui.component.input;

/**
 * The input type="password" component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class HiddenText<T> extends Text<T> {
	public HiddenText(Class<T> inputClass) {
		super(inputClass);
		setCssClass("ui-hit");
	}

	@Override
	public String getInputType() {
		return "password";
	}
}
