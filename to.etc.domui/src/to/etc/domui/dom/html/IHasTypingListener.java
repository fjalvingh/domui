package to.etc.domui.dom.html;

public interface IHasTypingListener {
	ITypingListener< ? > getOnTyping();

	void setOnTyping(ITypingListener< ? > onTypingListener);

}
