package to.etc.domui.dom.html;

public interface IValueChanged<T extends NodeBase> {
	void onValueChanged(T component) throws Exception;
}
