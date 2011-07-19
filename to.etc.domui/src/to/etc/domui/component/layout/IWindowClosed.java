package to.etc.domui.component.layout;

import javax.annotation.*;

public interface IWindowClosed {
	void closed(@Nonnull String closeReason) throws Exception;
}
