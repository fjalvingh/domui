package to.etc.domui.dom.html;

public interface ITypingListener<T extends NodeBase> {
	void onTyping(T component, boolean done) throws Exception;
}
