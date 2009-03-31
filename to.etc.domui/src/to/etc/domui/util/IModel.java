package to.etc.domui.util;

public interface IModel<T> extends IReadOnlyModel<T> {
	public void		setValue(T value) throws Exception;
}
