package to.etc.domui.util;

public interface IValueAccessor<T> extends IValueTransformer<T> {
	public void		setValue(Object target, T value) throws Exception;
}
