package to.etc.domui.util.asyncdialog;

import to.etc.util.Progress;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IAsyncTask {
	void execute(@Nonnull Progress progress) throws Exception;

}
