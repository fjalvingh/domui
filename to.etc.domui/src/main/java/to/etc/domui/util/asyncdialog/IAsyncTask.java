package to.etc.domui.util.asyncdialog;

import to.etc.domui.component.delayed.IProgress;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IAsyncTask {
	void execute(@Nonnull IProgress progress) throws Exception;

}
