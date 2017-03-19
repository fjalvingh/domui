package to.etc.domui.component.binding;

import to.etc.domui.dom.errors.*;

import javax.annotation.*;

/**
 * This represents a single "binding", a thing that needs to be updated at server entry
 * and server exit.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/10/14.
 */
public interface IBinding {
	@Nullable
	BindingValuePair<?, ?> getBindingDifference() throws Exception;

	void moveModelToControl() throws Exception;

	@Nullable
	UIMessage getBindError();

	/**
	 * Put the specified value into the bound model property.
	 * @param value
	 * @param <T>
	 */
	<T> void setModelValue(@Nullable T value);
}
