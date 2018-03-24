package to.etc.domui.dom.html;

import javax.annotation.*;

public interface INodeEvent<T> {
	void handle(@Nonnull T event) throws Exception;
}
