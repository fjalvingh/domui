package to.etc.domui.component2.form4;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component2.form4.FormBuilder.IAppender;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-3-18.
 */
public interface IFormLayouter extends IAppender {
	void setHorizontal(boolean horizontal);

	void addControl(@NonNull NodeBase control, @Nullable NodeContainer lbl, @Nullable String hintText, @Nullable String controlCss, @Nullable String labelCss, boolean append,
		BiConsumer<NodeContainer, String> hintRenderer);

	void clear();

	void appendAfterControl(NodeBase what);
}
