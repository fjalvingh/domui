package to.etc.formbuilder.pages;

public class ColorPropertyEditorFactory implements IPropertyEditorFactory {
	static public final ColorPropertyEditorFactory INSTANCE = new ColorPropertyEditorFactory();

	@Override
	public IPropertyEditor createEditor(PropertyDefinition pd) {
		return new ColorPropertyEditor(pd);
	}

}
