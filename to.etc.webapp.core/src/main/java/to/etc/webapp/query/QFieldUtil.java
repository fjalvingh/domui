package to.etc.webapp.query;

/**
 * @author <a href="mailto:dennis">Dennis</a>
 * Created on 2-4-18.
 */

public final class QFieldUtil {

	public static final String getName(QField<?,?> qField){
		return qField.getName();
	}
	public static final <T> Class<T> getRootClass(QField<T,?> qField){
		return qField.getRootClass();
	}
	public static final String getPropertyName(QField<?,?> qField){
		return qField.getPropertyName();
	}
}
