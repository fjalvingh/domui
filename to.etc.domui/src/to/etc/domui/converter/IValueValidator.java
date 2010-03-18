package to.etc.domui.converter;

/**
 * Defines a functor which validates a given value. The value can be of any type, as the validator
 * for input is usually called <i>after</i> conversion of the input to the base type. Validators for
 * primitive types must be defined as their wrapper. When a validator determines that validation fails
 * it dies by throwing a ValidationFailedException. This exception contains an error code AND parameters
 * for that code. These define the error message that will be thrown.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2008
 */
public interface IValueValidator<T> {
	public void validate(T input) throws Exception;
}
