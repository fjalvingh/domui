package to.etc.util;


public interface IExceptionClassifier<T extends Throwable> {
	public boolean isSevereException(T throwable);
}