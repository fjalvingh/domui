package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IPropertyEditor {

	public void setValue(@Nullable Object value);

	public void renderValue(@Nonnull NodeContainer target) throws Exception;

}
