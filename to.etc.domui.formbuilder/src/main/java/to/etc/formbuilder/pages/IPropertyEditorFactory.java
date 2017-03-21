package to.etc.formbuilder.pages;

import javax.annotation.*;

public interface IPropertyEditorFactory {
	@Nonnull
	public IPropertyEditor createEditor(@Nonnull PropertyDefinition pd);
}
