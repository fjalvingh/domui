package to.etc.domui.server;

import javax.annotation.*;

public interface IReloadListener {
	void reloaded(@Nonnull ClassLoader newloader) throws Exception;
}
