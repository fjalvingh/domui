package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IPropertyEditor {

	void setValue(@Nullable Object value);

	void renderValue(@Nonnull NodeContainer target) throws Exception;

}
