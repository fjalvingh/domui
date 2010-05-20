package to.etc.webapp.query;

public interface ICriteriaTableDef<T> {
	/**
	 * Returns the resulting type for queries.
	 * @return
	 */
	Class<T> getDataClass();

}
