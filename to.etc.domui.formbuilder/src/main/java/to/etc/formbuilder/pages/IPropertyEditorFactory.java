package to.etc.formbuilder.pages;

import javax.annotation.*;

public interface IPropertyEditorFactory {
	@Nonnull IPropertyEditor createEditor(@Nonnull PropertyDefinition pd);
}
