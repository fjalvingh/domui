package to.etc.domui.server;

import javax.annotation.*;

public interface IReloadListener {
	public void reloaded(@Nonnull ClassLoader newloader) throws Exception;
}
