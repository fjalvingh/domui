package to.etc.domui.component.builder;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface INodeTarget {
	void add(@Nonnull NodeContainer content);
}
