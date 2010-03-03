package to.etc.domui.converter;

/**
 * Represents a interface for convertable class.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Mar 2, 2010
 */
public interface IConvertable<T> {
	/**
	 * Sets the Converter to use to convert the value to a string.
	 * @param converter
	 */
	void setConverter(IConverter<T> converter);

	/**
	 * Returns the class of the converter.
	 * @return
	 */
	IConverter<T> getConverter();
}
