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
	public void moveControlToModel() throws Exception;

	public void moveModelToControl() throws Exception;

	@Nullable
	public UIMessage getBindError();
}
