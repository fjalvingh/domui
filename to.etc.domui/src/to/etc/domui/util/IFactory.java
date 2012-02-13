package to.etc.domui.util;

public interface IFactory<T> {
	T createInstance() throws Exception;
}
