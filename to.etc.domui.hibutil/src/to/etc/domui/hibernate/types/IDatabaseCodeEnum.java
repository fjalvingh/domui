package to.etc.domui.hibernate.types;

import javax.annotation.*;

/**
 * Must be implemented for enum types that need mapping from database column values to
 * enum labels. In combination with the {@link MappedEnumType} user type this allows enums
 * to be used for database values having an invalid content for an enum label name (like
 * numbers).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 18, 2009
 */
public interface IDatabaseCodeEnum {
	/**
	 * This returns the database-side of the presentation of this enum, i.e. the code string that is to be
	 * put in the database for a given enum label. All members must return a disjoint set (no two members may
	 * return the same code).
	 * @return
	 */
	@Nonnull
	String getCode();
}
