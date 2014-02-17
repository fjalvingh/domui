package to.etc.domui.dom.html;

import javax.annotation.*;

public interface INodeEvent<T> {
	public void handle(@Nonnull T event) throws Exception;
}
