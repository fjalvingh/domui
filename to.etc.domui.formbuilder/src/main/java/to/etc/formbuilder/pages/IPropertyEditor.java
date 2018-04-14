package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeContainer;

public interface IPropertyEditor {

	void setValue(@Nullable Object value);

	void renderValue(@NonNull NodeContainer target) throws Exception;

}
