package to.etc.domui.dom.html;

import javax.annotation.*;


/**
 * Used to trigger events when some value is selected and passed to registered listener.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Oct 2011
 */
public interface IValueSelected<T> {

	/**
	 * Pass selected value to listener.
	 * @param value can be null
	 * @throws Exception
	 */
	void valueSelected(@Nullable T value) throws Exception;
}
