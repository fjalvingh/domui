package to.etc.domui.dom.html;

public interface IInputNode<T> extends IInputBase {
	public T		getValue();
	public void		setValue(T v);
	public boolean	isReadOnly();
	public void		setReadOnly(boolean ro);
	public boolean	isMandatory();
	public void		setMandatory(boolean ro);
}
