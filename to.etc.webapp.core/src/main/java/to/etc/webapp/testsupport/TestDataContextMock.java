package to.etc.webapp.testsupport;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * Special mock data context that should be used for tests that never hit the database.
 * Use register methods to mock query results.m
 * Get inserted instances during logic run in case you need to check for inserted data.
 *
 * JENKINSTODO move class to test resource location once Jenkins maven build is fixed. This is not production code.
 *
 * Created by vmijic on 11.6.15..
 */
@DefaultNonNull
public class TestDataContextMock extends TestDataContextStub {

	/**
	 * Adds custom data decoration for objects that are being 'saved' inside mock context.
	 */
	public interface ISavedObjectDecorator{
		void decorate(Object savedObject);
	}

	private Map<String, List<Object[]>> m_querySelectionMap = new HashMap<String, List<Object[]>>();

	private Map<String, List< ? >> m_queryCriteriaMap = new HashMap<String, List< ? >>();

	private Map<String, Object> m_queryOneCriteriaMap = new HashMap<String, Object>();

	private Map<String, Object> m_queryOneSelectionMap = new HashMap<String, Object>();

	public static final String NULL = "[null]";

	private static long m_genericMockInsertId = Long.MAX_VALUE;

	private Long getUniqueId(){
		return m_genericMockInsertId--;
	}

	private HashMap<Long, IIdentifyable<Long>> m_insertedByLongId = new HashMap<Long, IIdentifyable<Long>>();

	private List<Object> m_inserted = new ArrayList<Object>();

	private List<Object> m_deleted = new ArrayList<Object>();

	@Nullable
	private final ISavedObjectDecorator m_onSaveCallback;

	public TestDataContextMock(@Nullable ISavedObjectDecorator onSaveCallback){
		m_onSaveCallback = onSaveCallback;
	}

	@DefaultNonNull
	final static private class EntityKey {
		private final Class<?> m_entityClass;

		private final Object m_id;

		public EntityKey(Class<?> entityClass, Object id) {
			m_entityClass = entityClass;
			m_id = id;
		}

		public Class<?> getEntityClass() {
			return m_entityClass;
		}

		public Object getId() {
			return m_id;
		}

		@Override
		public boolean equals(@Nullable Object o) {
			if(this == o)
				return true;
			if(o == null || getClass() != o.getClass())
				return false;

			EntityKey entityKey = (EntityKey) o;

			if(!m_entityClass.equals(entityKey.m_entityClass))
				return false;
			return m_id.equals(entityKey.m_id);

		}

		@Override
		public int hashCode() {
			int result = m_entityClass.hashCode();
			result = 31 * result + m_id.hashCode();
			return result;
		}
	}

	static final private class EntityInstance {
		private final Object m_entity;

		private boolean m_inserted;

		private boolean m_deleted;

		public EntityInstance(Object entity) {
			m_entity = entity;
		}

		public Object getEntity() {
			return m_entity;
		}

		public boolean isInserted() {
			return m_inserted;
		}

		public void setInserted(boolean inserted) {
			m_inserted = inserted;
		}

		public boolean isDeleted() {
			return m_deleted;
		}

		public void setDeleted(boolean deleted) {
			m_deleted = deleted;
		}
	}

	private final Map<EntityKey, EntityInstance> m_entityByIdMap = new HashMap<>();

	@Override
	public void save(final Object o) throws Exception {
		PropertyInfo idProp = ClassUtil.findPropertyInfo(o.getClass(), "id");
		if(null == idProp) {
			System.err.println("mockdc: trying to save " + o.getClass() + " which has no 'id' primary key property, ignored");
			return;
		}
		Object pk = idProp.getValue(o);
		if(null == pk) {
			//-- We need to assign one.
			pk = Long.valueOf(getUniqueId());
			assignPk(o, idProp, pk);
		}

		if(null != pk) {
			registerInstance(o, pk);
		}

		if (!m_inserted.contains(o)){
			m_inserted.add(o);

			//-- If there is a ros_id: make sure it has a value
			ISavedObjectDecorator decorator = m_onSaveCallback;
			if (null != decorator) {
				decorator.decorate(o);
			}

		}
	}

	private void registerInstance(Object o, Object pk) {
		EntityKey key = new EntityKey(o.getClass(), pk);
		EntityInstance instance = m_entityByIdMap.get(key);
		if(null == instance) {
			instance = new EntityInstance(o);
			m_entityByIdMap.put(key, instance);
			instance.setInserted(true);
		} else {
			if(instance.getEntity() != o) {
				throw new IllegalStateException("mockdb: trying to save different instances with the same primary key " + pk +" (class " + o.getClass() +")");
			}
		}
	}

	private void assignPk(Object o, PropertyInfo idProp, Object id) throws Exception {
		if(idProp.getActualType() != Long.class) {
			System.err.println("mockdc: trying to save " + o.getClass() + " with a non-Long 'id' property that is null- ignored");
		} else {
			Method setter = idProp.getSetter();
			if(null == setter) {
				System.err.println("mockdc: trying to save " + o.getClass() + " whose 'id' property has no setter- ignored");
				return;
			}

			setter.invoke(o, id);
			if(id instanceof Long) {
				m_insertedByLongId.put((Long) id, (IIdentifyable<Long>) o);
			}
			m_inserted.add(o);
		}
	}

	@Override
	public void delete(final Object o) throws Exception {
		m_deleted.add(o);
		m_inserted.remove(o);
		if (m_insertedByLongId.values().contains(o)){
			IIdentifyable<Long> longIdentifiable = (IIdentifyable<Long>) o;
			m_insertedByLongId.remove(longIdentifiable.getId());
		}
	}

	@Nonnull
	@Override
	public <T> T get(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		T item = find(clz, pk);
		if (null == item) {
			throw new IllegalStateException("Not located object of class " + clz + " with ID: " + pk);
		}
		return item;
	}

	@Nonnull
	@Override
	public <T> T getInstance(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		T item = find(clz, pk);
		if(null != item)
			return item;

		item = clz.newInstance();
		PropertyInfo idProp = ClassUtil.findPropertyInfo(clz, "id");
		if(null == idProp) {
			throw new IllegalStateException("mockdc: trying to getInstance " + clz + " which has no 'id' primary key property");
		}
		assignPk(item, idProp, pk);
		registerInstance(item, pk);
		return item;
	}

	@Nullable
	@Override
	public <T> T find(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		EntityKey key = new EntityKey(clz, pk);
		EntityInstance instance = m_entityByIdMap.get(key);
		if(null != instance) {
			Object entity = instance.getEntity();
			if(! clz.isAssignableFrom(entity.getClass()))
				throw new IllegalStateException("The returned class " + entity.getClass() + " is not assignable to the requested class type " + clz);
			return (T) entity;
		}
		return null;
	}

	@Override
	public List<Object[]> query(QSelection<?> sel) throws Exception {
		String testId = checkTestsId(sel);
		return processSelectionTestId(testId);
	}

	@Override
	public <T> List<T> query(QCriteria<T> q) throws Exception {
		String testId = checkTestsId(q);
		return processQueryTestId(testId);
	}

	@Nullable
	@Override
	public <T> T queryOne(@Nonnull QCriteria<T> q) throws Exception {
		String testId = checkTestsId(q);
		return processQueryOneTestId(testId);
	}

	@Nullable
	@Override
	public Object[] queryOne(@Nonnull QSelection<?> sel) throws Exception {
		String testId = checkTestsId(sel);
		return processSelectionOneTestId(testId);
	}

	protected <T> List<T> processQueryTestId(String testId) {
		List< ? > res = m_queryCriteriaMap.get(testId);
		if (null == res){
			throw new IllegalStateException("Not defined criteria query result for testId: " + testId);
		}
		return ((List<T>)res);
	}

	@Nullable
	protected <T> T processQueryOneTestId(String testId) {
		Object res = m_queryOneCriteriaMap.get(testId);
		if (null == res){
			throw new IllegalStateException("Not defined criteria query one result for testId: " + testId);
		}else if (NULL.equals(res)){
			return null;
		}else{
			return (T)res;
		}
	}

	protected List<Object[]> processSelectionTestId(String testId) {
		List<Object[]> res = m_querySelectionMap.get(testId);
		if (null == res){
			throw new IllegalStateException("Not defined selection query result for testId: " + testId);
		}
		return res;
	}

	@Nullable
	protected Object[] processSelectionOneTestId(String testId) {
		Object res = m_queryOneSelectionMap.get(testId);
		if (null == res){
			throw new IllegalStateException("Not defined selection query one result for testId: " + testId);
		}else if (NULL.equals(res)){
			return null;
		}else{
			return (Object[])res;
		}
	}

	public void registerQuery(String testId, List<?> criteriaResult){
		m_queryCriteriaMap.put(testId, criteriaResult);
	}

	/**
	 * Enables specifying fallback testId mocks, only in case when previously it is not defined.
	 * @param override when T - overrides already existing mocks, otherwise keep existing value.
	 * @param testId
	 * @param criteriaResult
	 */
	public void registerQuery(boolean override, String testId, List<?> criteriaResult){
		if (!override && null != m_queryCriteriaMap.get(testId)){
			return;
		}
		registerQuery(testId, criteriaResult);
	}

	public void registerSelection(String testId, List<Object[]> selectionResult){
		m_querySelectionMap.put(testId, selectionResult);
	}

	/**
	 * Enables specifying fallback testId mocks, only in case when previously it is not defined.
	 * @param override when T - overrides already existing mocks, otherwise keep existing value.
	 * @param testId
	 * @param selectionResult
	 */
	public void registerSelection(boolean override, String testId, List<Object[]> selectionResult){
		if (!override && null != m_querySelectionMap.get(testId)){
			return;
		}
		registerSelection(testId, selectionResult);
	}

	public void registerQueryOne(String testId, @Nullable Object queryOneResult){
		m_queryOneCriteriaMap.put(testId, null == queryOneResult ? NULL : queryOneResult);
	}

	/**
	 * Enables specifying fallback testId mocks, only in case when previously it is not defined.
	 * @param override when T - overrides already existing mocks, otherwise keep existing value.
	 * @param testId
	 * @param queryOneResult
	 */
	public void registerQueryOne(boolean override, String testId, @Nullable Object queryOneResult){
		if (!override && null != m_queryOneCriteriaMap.get(testId)){
			return;
		}
		registerQueryOne(testId, queryOneResult);
	}

	public void registerSelectionOne(String testId, @Nullable Object... selectionResult){
		m_queryOneSelectionMap.put(testId, null == selectionResult ? NULL : selectionResult);
	}

	/**
	 * Enables specifying fallback testId mocks, only in case when previously it is not defined.
	 * @param override when T - overrides already existing mocks, otherwise keep existing value.
	 * @param testId
	 * @param selectionResult
	 */
	public void registerSelectionOne(boolean override, String testId, @Nullable Object... selectionResult){
		if (!override && null != m_queryOneSelectionMap.get(testId)){
			return;
		}
		registerSelectionOne(testId, selectionResult);
	}
	private String checkTestsId(QCriteriaQueryBase< ? > cqb) {
		String testId = cqb.getTestId();
		if (null == testId){
			throw new IllegalStateException("No testId defined for: " + cqb);
		}
		return testId;
	}

	public <T> List<T> getInserted(Class<T> type){
		List<T> inserted = new ArrayList<T>();
		for (Object value : m_inserted){
			if (value.getClass().isAssignableFrom(type)){
				inserted.add((T) value);
			}
		}
		return inserted;
	}

	public <T> List<T> getDeleted(Class<T> type){
		List<T> deleted = new ArrayList<T>();
		for (Object value : m_deleted){
			if (value.getClass().isAssignableFrom(type)){
				deleted.add((T) value);
			}
		}
		return deleted;
	}

	/**
	 * Clears accumulated list of inserted/deleted objects. Use between test methods when context is shared between methods.
	 * jal: why is is needed? Why not create a new one- that is way less prone to errors.
	 */
	public void clear(){
		m_insertedByLongId.clear();
		m_inserted.clear();
		m_deleted.clear();
		m_querySelectionMap.clear();
		m_querySelectionMap.clear();
		m_queryOneCriteriaMap.clear();
		m_queryOneSelectionMap.clear();
		m_entityByIdMap.clear();
	}
}
