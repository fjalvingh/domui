package to.etc.domui.dom.html;

/**
 * Interface to listener for lookup typing event. 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 29 Jan 2010
 */
public interface ILookupTypingListener<T extends NodeBase> {
	void onLookupTyping(T component, boolean done) throws Exception;
}
