package to.etc.domui.component.meta;

import javax.annotation.*;

@DefaultNonNull
public class MetaDuplicator<T> extends MetaObjectCopyBase<T> {
	public MetaDuplicator(T source) {
		super(source, Mode.SHALLOW);
	}


	public T build() throws Exception {
		StringBuilder sb = new StringBuilder();
		T copy = cloneInstance(getSource(), sb);
		return copy;
	}

	/**
	 * Specify which relation properties need a "deep" copy, where the relation itself is
	 * also copied. For list properties this implies that all elements of the list are
	 * copied too.
	 *
	 * @param properties
	 * @return
	 */
	public MetaDuplicator<T> deep(String... properties) {
		setProperties(Mode.DEEP, properties);
		return this;
	}

	public MetaDuplicator<T> shallow(String... properties) {
		setProperties(Mode.SHALLOW, properties);
		return this;
	}

	public MetaDuplicator<T> copy(String... properties) {
		setProperties(Mode.COPY, properties);
		return this;
	}

	@Override
	public MetaDuplicator<T> ignore(String... properties) {
		ignore(properties);
		return this;
	}

	/**
	 * Specify a set of properties that should be the only ones copied.
	 * @param properties
	 * @return
	 */
	public MetaDuplicator<T> only(String... properties) {
		setOnly(properties);
		return this;
	}


}
