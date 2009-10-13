package to.etc.domui.component.input;

/**
 * EXPERIMENTAL - DO NOT USE.
 * This defines the component as an input component that can be bound to some
 * value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBindable {
	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * Return the object that is able to bind this control to some data value.
	 * @return
	 */
	IBinder bind();

	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * If this object is actually bound to something return true.
	 *
	 * @return
	 */
	boolean isBound();
}
