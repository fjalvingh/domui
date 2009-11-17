package to.etc.domui.converter;


/**
 * Represents a converter pair, meaning something which can convert round-trip.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public interface IConverter<T> extends IObjectToStringConverter<T>, IStringToObjectConverter<T> {
}
