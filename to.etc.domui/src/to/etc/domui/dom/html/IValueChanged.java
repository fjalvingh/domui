package to.etc.domui.dom.html;

public interface IValueChanged<T extends NodeBase, V> {
	public void		onValueChanged(T component, V value) throws Exception;
}
