package to.etc.domui.util.db;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.qsql.JdbcUtil;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final public class BatchUtil {

	private BatchUtil() {}

	/**
	 * Does bulk delete using JDBC. Use it when performance matters ;)
	 * @throws SQLException
	 */
	public static <K extends Number, T extends IIdentifyable<K>> boolean bulkDelete(@NonNull Connection con, ClassMetaModel cmm, @NonNull List<T> items) throws SQLException {
		final PropertyMetaModel<?> pkPmm = cmm.getPrimaryKey();
		if(null == pkPmm) {
			throw new IllegalArgumentException("Unsupported working with class models that do not have PK columns! For " + cmm.getTableName() + " fund no PK!");
		}
		String[] pkCols = pkPmm.getColumnNames();
		if(pkCols.length != 1) {
			throw new IllegalArgumentException("Unsupported working with class models that use Composite PK columns! For " + cmm.getTableName() + " fund PK: " + pkCols);
		}
		String pkCol = pkCols[0];
		boolean result = true;
		for(List<K> idsChunk: Iterables.partition(items.stream().map(it -> it.getSafeId()).collect(Collectors.toList()), 1000)) {
			String idsValues = idsChunk.stream().map(it -> "" + it).collect(Collectors.joining(","));
			result = result && JdbcUtil.executeStatement(con, "delete from " + cmm.getTableName() + " where " + pkCol + " in (" + idsValues + ")");
		}
		return result;
	}

	/**
	 * Does bulk insert using JDBC. Use it when performance matters ;)
	 * @throws Exception
	 */
	public static <K, T extends IIdentifyable<K>> int bulkInsert(@NonNull Connection con, ClassMetaModel cmm, @NonNull List<T> items, QField<T, ?>... fields) throws Exception {

		PropertyMetaModel<?>[] props = new PropertyMetaModel<?>[fields.length];
		Arrays.stream(fields).map(it -> cmm.getProperty(it)).collect(Collectors.toList()).toArray(props);

		String insertColumns = Arrays.stream(props).map(it -> it.getColumnNames()[0]).collect(Collectors.joining(","));
		String params = Arrays.stream(props).map(it -> "?").collect(Collectors.joining(","));
		String sql = "insert into " + cmm.getTableName() + " (" + insertColumns + ") values (" + params + ")";
		int inserted = 0;
		try(PreparedStatement ps = con.prepareStatement(sql)){
			for(List<T> chunk : Iterables.partition(items, 10000)) {
				for(T item: chunk) {
					for(int index = 0; index < props.length; index++) {
						Object value = props[index].getValue(item);
						if(value instanceof IIdentifyable<?>) {
							value = ((IIdentifyable<?>)value).getId();
						}
						JdbcUtil.setParameter(ps, value, index + 1);
					}
					ps.addBatch();
				}
				inserted += ps.executeBatch().length;
				ps.clearBatch();
			}
		}
		return inserted;
	}

}
