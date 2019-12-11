package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.core.IRunnable;

import java.sql.Connection;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-12-18.
 */
public class QDataContextWrapper implements QDataContext {
	private final QDataContext m_source;

	public QDataContextWrapper(QDataContext source) {
		m_source = source;
	}

	@Override @NonNull public QDataContextFactory getFactory() {
		return m_source.getFactory();
	}

	@Override public void setIgnoreClose(boolean on) {
		m_source.setIgnoreClose(on);
	}

	@Override public void close() {
		m_source.close();
	}

	@Override @NonNull public <T> List<T> query(QCriteria<T> q) throws Exception {
		return m_source.query(q);
	}

	@Override @Nullable public <T> T queryOne(QCriteria<T> q) throws Exception {
		return m_source.queryOne(q);
	}

	@Override @NonNull public List<Object[]> query(QSelection<?> sel) throws Exception {
		return m_source.query(sel);
	}

	@Override @NonNull public <R> List<R> query(Class<R> resultInterface, QSelection<?> sel) throws Exception {
		return m_source.query(resultInterface, sel);
	}

	@Override @Nullable public Object[] queryOne(QSelection<?> q) throws Exception {
		return m_source.queryOne(q);
	}

	@Override @Nullable public <R> R queryOne(Class<R> resultInterface, QSelection<?> sel) throws Exception {
		return m_source.queryOne(resultInterface, sel);
	}

	@Override @Nullable public <T> T find(Class<T> clz, Object pk) throws Exception {
		return m_source.find(clz, pk);
	}

	@Override @NonNull public <T> T get(Class<T> clz, Object pk) throws Exception {
		return m_source.get(clz, pk);
	}

	@Override @Nullable public <T> T find(ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		return m_source.find(metatable, pk);
	}

	@Override @Nullable public <T> T original(T copy) {
		return m_source.original(copy);
	}

	@Override public void setKeepOriginals() {
		m_source.setKeepOriginals();
	}

	@Override @NonNull public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		return m_source.getInstance(clz, pk);
	}

	@Override @NonNull public <T> T getInstance(ICriteriaTableDef<T> clz, Object pk) throws Exception {
		return m_source.getInstance(clz, pk);
	}

	@Override public void attach(Object o) throws Exception {
		m_source.attach(o);
	}

	@Override public void save(Object o) throws Exception {
		m_source.save(o);
	}

	@Override public void refresh(Object o) throws Exception {
		m_source.refresh(o);
	}

	@Override public void delete(Object o) throws Exception {
		m_source.delete(o);
	}

	@Override public void startTransaction() throws Exception {
		m_source.startTransaction();
	}

	@Override public void commit() throws Exception {
		m_source.commit();
	}

	@Override public void rollback() throws Exception {
		m_source.rollback();
	}

	@Override public boolean inTransaction() throws Exception {
		return m_source.inTransaction();
	}

	@Override @NonNull public Connection getConnection() throws Exception {
		return m_source.getConnection();
	}

	@Override public void addCommitAction(IRunnable cx) {
		m_source.addCommitAction(cx);
	}

	@Override public void addListener(IQDataContextListener qDataContextListener) {
		m_source.addListener(qDataContextListener);
	}

	@Override @NonNull public <T> T reload(T source) throws Exception {
		return m_source.reload(source);
	}

	public QDataContext getWrapped() {
		return m_source;
	}

	@Nullable
	@Override
	public <T> T getAttribute(Class<T> property) {
		return m_source.getAttribute(property);
	}

	@Override
	public <T> void setAttribute(Class<T> tClass, T value) {
		m_source.setAttribute(tClass, value);
	}
}
