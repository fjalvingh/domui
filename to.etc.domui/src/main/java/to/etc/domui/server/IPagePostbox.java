package to.etc.domui.server;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-5-19.
 */
public interface IPagePostbox {
	boolean acceptMessage(Class<?> messageClass);
}
