package to.etc.domui.component.layout;

/**
 * Things that are floating above the UI need this interface so their stacking
 * level can be controlled. See {@link FloatingWindow} and {@link Dialog} for
 * examples.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public interface IFloating {
	int getZIndex();

	void setZIndex(int index);

	boolean isModal();


}
