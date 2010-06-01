package to.etc.domui.component.meta;

import javax.annotation.*;

/**
 * The root for all metamodel lookups. When fields of known classes are
 * used in the system this can be used to lookup data pertaining to the
 * fields.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public interface IClassMetaModelFactory {
	/**
	 * Must return a value &gt; 0 when this knows how to create a metamodel for the specified thingerydoo.
	 *
	 * @param theThingy
	 * @return
	 */
	int accepts(@Nonnull Object theThingy);

	/**
	 * When accept() has returned a &gt; 0 value, this <i>must</i> create an (immutable) metamodel for
	 * the thingy passed.
	 *
	 * @param theThingy
	 * @return
	 */
	@Nonnull
	ClassMetaModel createModel(@Nonnull Object theThingy);
}
