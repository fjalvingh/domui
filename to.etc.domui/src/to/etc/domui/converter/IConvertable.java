package to.etc.domui.converter;

/**
 * Represents a interface for a component that accepts a converter to convert a value to T.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Mar 2, 2010
 */
public interface IConvertable<T> {
	/**
	 * Sets the Converter to use to convert the value to a string and vice versa.
	 * @param converter
	 */
	void setConverter(IConverter<T> converter);

	/**
	 * Returns the class of the converter.
	 * @return
	 */
	IConverter<T> getConverter();
}
