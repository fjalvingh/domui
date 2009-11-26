package to.etc.domui.dom.html;

/**
 * The fence that collects user data modification indications from child components. An modified fence remeber if
 * change was done on controls that are later removed.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 4 Nov 2009
 */
public interface IUserInputModifiedFence extends IHasModifiedIndication {
	boolean isFinalUserInputModifiedFence();
}
