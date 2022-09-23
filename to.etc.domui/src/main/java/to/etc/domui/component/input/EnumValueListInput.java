package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.DomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Control that defines list of input variables that have specified enum as its value.
 * Can be rendered as list dropdown inputs or list of radio button inputs.
 * Allows customization renderers for both rendering variable parts and enum parts.
 *
 * @param <V> Type of variable objects
 * @param <E> Enum that defines value of variables
 */
@NonNullByDefault
public class EnumValueListInput<V, E extends Enum<E>> extends EnumValueListInputBase<V, E> {

	public EnumValueListInput(Class<E> type, @Nullable E defaultValue) {
		super(type, defaultValue);
	}

	public EnumValueListInput(Class<E> type, @Nullable E defaultValue, List<V> data) {
		super(type, defaultValue, data);
	}

	@Override
	public EnumValueListInput<V, E> css(String... css) {
		super.css(css);
		return this;
	}


	@Override
	public EnumValueListInput<V, E> asButtons() {
		super.asButtons();
		return this;
	}

	static public <V extends Enum<V>, E extends Enum<E>> EnumValueListInput<V, E> create(Class<V> clz, Class<E> valueEnum, E defaultValue, V... exceptions) {
		V[] ar = clz.getEnumConstants();
		List<V> data = new ArrayList<>();
		for(V v : ar) {
			if(!DomUtil.contains(exceptions, v)) {
				data.add(v);
			}
		}
		return new EnumValueListInput<V, E>(valueEnum, defaultValue, data);
	}
}
