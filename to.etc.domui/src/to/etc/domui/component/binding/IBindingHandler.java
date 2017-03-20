package to.etc.domui.component.binding;

/**
 * Handles binding of data during request entry and exit.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
public interface IBindingHandler {
	void controlToModel() throws Exception;
	void modelToControl() throws Exception;
}
