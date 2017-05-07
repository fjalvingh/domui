package to.etc.domui.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-5-17.
 */
public interface IWriteOnlyModel<T> {
	void getValue(T value) throws Exception;
}
