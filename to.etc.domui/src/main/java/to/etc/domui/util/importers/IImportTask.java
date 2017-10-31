package to.etc.domui.util.importers;

import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.dom.html.NodeContainer;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportTask<R> {
	R execute(File input, IProgress progress) throws Exception;

	void onComplete(NodeContainer node, R result);
}
