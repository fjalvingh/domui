package to.etc.domui.state;

public interface IConversationStateListener {
	public void conversationNew(ConversationContext cc) throws Exception;

	public void conversationAttached(ConversationContext cc) throws Exception;

	public void conversationDetached(ConversationContext cc) throws Exception;

	public void conversationDestroyed(ConversationContext cc) throws Exception;
}
