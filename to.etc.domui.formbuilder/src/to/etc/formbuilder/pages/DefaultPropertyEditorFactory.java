package to.etc.formbuilder.pages;

public class DefaultPropertyEditorFactory implements IPropertyEditorFactory {
	static public final DefaultPropertyEditorFactory INSTANCE = new DefaultPropertyEditorFactory();

	@Override
	public IPropertyEditor createEditor(PropertyDefinition pd) {
		return new DefaultPropertyEditor(pd);
	}

}
