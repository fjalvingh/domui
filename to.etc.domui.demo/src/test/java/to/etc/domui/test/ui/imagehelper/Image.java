package to.etc.domui.test.ui.imagehelper;

/**
 * A black-and-white image
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-8-17.
 */
public interface Image {
	int width();

	int height();

	int get(int x, int y);
}
