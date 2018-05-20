package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;

public interface IPropertyEditorFactory {
	@NonNull IPropertyEditor createEditor(@NonNull PropertyDefinition pd);
}
