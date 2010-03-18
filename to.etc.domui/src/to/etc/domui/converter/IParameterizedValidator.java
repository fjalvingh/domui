package to.etc.domui.converter;

public interface IParameterizedValidator<T> extends IValueValidator<T> {
	void setParameters(String[] parameters);
}
