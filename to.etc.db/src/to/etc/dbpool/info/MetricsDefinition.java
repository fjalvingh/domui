package to.etc.dbpool.info;

import java.util.*;

import javax.annotation.*;

import to.etc.dbpool.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/2/16.
 */
@DefaultNonNull
final public class MetricsDefinition {
	public static final int M_PHYSICAL_READS = 1;

	public static final int M_LOGICAL_READS = 2;

	public static final int M_LOGICAL_WRITES = 3;

	final private int m_id;

	final private String m_key;

	final private String m_label;

	final private IMetricValueTranslator m_translator;

	final private String m_description;

	final private boolean m_defined;

	static private Map<String, MetricsDefinition> m_definitionMap = new HashMap<>();

	private final int m_order;

	private final int[] m_minmax;

	static public class MetricBuilder {
		private int m_id = -1;

		final private String m_key;

		@Nullable
		private String m_label;

		@Nullable
		private String m_desc;

		@Nullable
		private IMetricValueTranslator m_xlat;

		private int m_order;

		private int[] m_minmax;

		public MetricBuilder(String key) {
			m_key = key;
		}

		public MetricBuilder id(int id) {
			m_id = id;
			return this;
		}

		public MetricBuilder d(String desc) {
			m_desc = desc;
			return this;
		}

		public MetricBuilder l(String lbl) {
			m_label = lbl;
			return this;
		}

		public MetricBuilder order(int order) {
			m_order = order;
			return this;
		}

		public MetricBuilder minmax(int... minmax) {
			m_minmax = minmax;
			return this;
		}

		public MetricBuilder xlat(IMetricValueTranslator xl) {
			m_xlat = xl;
			return this;
		}

		public void register() {
			IMetricValueTranslator xlat = m_xlat;
			if(xlat == null)
				xlat = (a) -> String.valueOf(a);

			MetricsDefinition.register(new MetricsDefinition(m_id, m_key, m_label == null ? m_key : m_label, xlat, m_desc == null ? m_key : m_desc, true, m_order, m_minmax));
		}
	}

	public MetricsDefinition(int id, String key, String label, IMetricValueTranslator translator, String description, boolean defined) {
		this(id, key, label, translator, description, defined, 100, new int[0]);
	}

	public MetricsDefinition(int id, String key, String label, IMetricValueTranslator translator, String description, boolean defined, int order, int[] minmax) {
		m_id = id;
		m_key = key;
		m_label = label;
		m_translator = translator;
		m_description = description;
		m_defined = defined;
		m_order = order;
		m_minmax = minmax;
	}

	static public synchronized void register(MetricsDefinition md) {
		m_definitionMap.put(md.getKey(), md);
	}

	static public void register(String key, String label, String description, IMetricValueTranslator tx) {
		register(new MetricsDefinition(0, key, label, tx, description, true));
	}
	static public void register(String key, String label, IMetricValueTranslator tx) {
		register(new MetricsDefinition(0, key, label, tx, label, true));
	}
	static public void register(String key, IMetricValueTranslator tx) {
		register(new MetricsDefinition(0, key, key, tx, key, true));
	}

	static public void register(int id, String key, String label, String description, IMetricValueTranslator tx) {
		register(new MetricsDefinition(id, key, label, tx, description, true));
	}
	static public void register(int id, String key, String label, IMetricValueTranslator tx) {
		register(new MetricsDefinition(id, key, label, tx, label, true));
	}
	static public void register(int id, String key, IMetricValueTranslator tx) {
		register(new MetricsDefinition(id, key, key, tx, key, true));
	}

	public String getKey() {
		return m_key;
	}

	public int getId() {
		return m_id;
	}

	public String getLabel() {
		return m_label;
	}

	public IMetricValueTranslator getTranslator() {
		return m_translator;
	}

	public String getDescription() {
		return m_description;
	}

	public static Map<String, MetricsDefinition> getDefinitionMap() {
		return m_definitionMap;
	}

	//static synchronized private MetricsDefinition get(String name) {
	//	return m_definitionMap.get(name);
	//}

	static {
		IMetricValueTranslator time = new IMetricValueTranslator() {
			@Override
			public String translate(double value) {
				return DbPoolUtil.strNanoTime((long) (value * 1000));
			}
		};

		IMetricValueTranslator num = new IMetricValueTranslator() {
			@Override
			public String translate(double value) {
				return DbPoolUtil.strCommad((long) value);
			}
		};

		IMetricValueTranslator bytes = (a) -> DbPoolUtil.strSize((long) a);

		IMetricValueTranslator tensofms = (a) -> DbPoolUtil.strNanoTime((long) a * 10 * 1000 * 1000);

		IMetricValueTranslator buffers = new IMetricValueTranslator() {
			@Override
			public String translate(double value) {
				long v = (long) value;
				return DbPoolUtil.strCommad(v) + " (" + DbPoolUtil.strSize(v * 8192) + ")";
			}
		};

		register("DB CPU", "Database CPU time", time);
		register("DB time", "Database time", "CPU time and wait time, accumulated", time);
		register("execute count", "Number of executes", num);
		register("opened cursors cumulative", "Total opened cursors", num);
		register("parse count (total)", num);
		register("parse time elapsed", time);
		register("session cursor cache hits", num);
		def("session logical reads").id(M_LOGICAL_READS).xlat(buffers).order(120).register();
		register("sql execute elapsed time", time);
		register("user I/O wait time", time);
		def("physical reads").id(M_PHYSICAL_READS).xlat(buffers).order(120).register();
		def("db block changes").id(M_LOGICAL_WRITES).xlat(buffers).order(120).register();
		register("redo size", bytes);

		//-- Get basic
		//def("Batched IO (bound) vector count").order(10).xlat(num).minmax().register();
		//def("Batched IO block miss count").order(10).xlat(num).minmax().register();
		//def("Batched IO double miss count").order(10).xlat(num).minmax().register();
		//def("Batched IO same unit count").order(10).xlat(num).minmax().register();
		//def("Batched IO single block count").order(10).xlat(num).minmax().register();
		//def("Batched IO slow jump count").order(10).xlat(num).minmax().register();
		//def("Batched IO vector block count").order(10).xlat(num).minmax().register();
		//def("Batched IO vector read count").order(10).xlat(num).minmax().register();
		//def("Block Cleanout Optim referenced").order(10).xlat(num).minmax().register();

		//def("CCursor + sql area evicted").order(10).xlat(num).minmax().register();
		def("CPU used by this session").order(10).xlat(tensofms).minmax().register();
		def("CPU used when call started").order(10).xlat(tensofms).minmax().register();
		def("CR blocks created").order(10).xlat(num).minmax().register();
		def("Commit SCN cached").order(10).xlat(num).minmax().register();
		//def("DB time").order(10).xlat(num).minmax().level(2).register(); DUPLICATE
		//def("DBWR checkpoint buffers written").order(10).xlat(num).minmax().register();
		//def("DBWR checkpoints").order(10).xlat(num).minmax().register();
		//def("DBWR object drop buffers written").order(10).xlat(num).minmax().register();
		//def("DBWR transaction table writes").order(10).xlat(num).minmax().register();
		//def("DBWR undo block writes").order(10).xlat(num).minmax().register();
		//def("HSC Heap Segment Block Changes").order(10).xlat(num).minmax().register();
		//def("Heap Segment Array Inserts").order(10).xlat(num).minmax().register();
		//def("Heap Segment Array Updates").order(10).xlat(num).minmax().register();
		//def("IMU Flushes").order(10).xlat(num).minmax().register();
		//def("IMU Redo allocation size").order(10).xlat(num).minmax().register();
		//def("IMU commits").order(10).xlat(num).minmax().register();
		//def("IMU contention").order(10).xlat(num).minmax().register();
		//def("IMU undo allocation size").order(10).xlat(num).minmax().register();
		//def("LOB table id lookup cache misses").order(10).xlat(num).minmax().register();
		//def("Number of read IOs issued").order(10).xlat(num).minmax().register();
		//def("OS Block input operations").order(10).xlat(num).minmax().register();
		//def("OS Block output operations").order(10).xlat(num).minmax().register();
		//def("OS Involuntary context switches").order(10).xlat(num).minmax().register();
		//def("OS Maximum resident set size").order(10).xlat(num).minmax().register();
		//def("OS Page reclaims").order(10).xlat(num).minmax().register();
		//def("OS System time used").order(10).xlat(num).minmax().register();
		//def("OS User time used").order(10).xlat(num).minmax().register();
		//def("OS Voluntary context switches").order(10).xlat(num).minmax().register();
		def("Requests to/from client").order(10).xlat(num).minmax().register();
		//def("RowCR attempts").order(10).xlat(num).minmax().register();
		//def("RowCR hits").order(10).xlat(num).minmax().register();
		//def("SMON posted for undo segment shrink").order(10).xlat(num).minmax().register();
		def("SQL*Net roundtrips to/from client").order(10).xlat(num).minmax().register();
		//def("TBS Extension: bytes extended").order(10).xlat(num).minmax().register();
		//def("TBS Extension: files extended").order(10).xlat(num).minmax().register();
		//def("TBS Extension: tasks executed").order(10).xlat(num).minmax().register();
		//def("active txn count during cleanout").order(10).xlat(num).minmax().register();
		//def("application wait time").order(10).xlat(num).minmax().register();
		//def("background checkpoints completed").order(10).xlat(num).minmax().register();
		//def("background checkpoints started").order(10).xlat(num).minmax().register();
		//def("background timeouts").order(10).xlat(num).minmax().register();
		//def("buffer is not pinned count").order(10).xlat(num).minmax().register();
		//def("buffer is pinned count").order(10).xlat(num).minmax().register();
		def("bytes received via SQL*Net from client").order(10).xlat(bytes).minmax().register();
		def("bytes sent via SQL*Net to client").order(10).xlat(bytes).minmax().register();
		//def("calls to get snapshot scn: kcmgss").order(10).xlat(num).minmax().register();
		//def("calls to kcmgas").order(10).xlat(num).minmax().register();
		//def("calls to kcmgcs").order(10).xlat(num).minmax().register();
		//def("cell physical IO interconnect bytes").order(10).xlat(num).minmax().register();
		//def("change write time").order(10).xlat(num).minmax().register();
		//def("cleanout - number of ktugct calls").order(10).xlat(num).minmax().register();
		//def("cleanouts and rollbacks - consistent read gets").order(10).xlat(num).minmax().register();
		//def("cleanouts only - consistent read gets").order(10).xlat(num).minmax().register();
		//def("cluster key scan block gets").order(10).xlat(num).minmax().register();
		//def("cluster key scans").order(10).xlat(num).minmax().register();
		//def("commit batch/immediate performed").order(10).xlat(num).minmax().register();
		//def("commit batch/immediate requested").order(10).xlat(num).minmax().register();
		//def("commit cleanouts").order(10).xlat(num).minmax().register();
		//def("commit cleanouts successfully completed").order(10).xlat(num).minmax().register();
		//def("commit immediate performed").order(10).xlat(num).minmax().register();
		//def("commit immediate requested").order(10).xlat(num).minmax().register();
		//def("commit txn count during cleanout").order(10).xlat(num).minmax().register();
		def("concurrency wait time").order(10).xlat(num).minmax().register();
		def("consistent changes").order(10).xlat(num).minmax().register();
		def("consistent gets").order(10).xlat(num).minmax().register();
		def("consistent gets - examination").order(10).xlat(num).minmax().register();
		def("consistent gets direct").order(10).xlat(num).minmax().register();
		def("consistent gets from cache").order(10).xlat(num).minmax().register();
		def("consistent gets from cache (fastpath)").order(10).xlat(num).minmax().register();
		//def("cursor authentications").order(10).xlat(num).minmax().register();
		def("data blocks consistent reads - undo records applied").order(10).xlat(num).minmax().register();
		//def("db block changes").order(10).xlat(buffers).minmax().register();
		def("db block gets").order(10).xlat(buffers).minmax().register();
		def("db block gets direct").order(10).xlat(buffers).minmax().register();
		def("db block gets from cache").order(10).xlat(buffers).minmax().register();
		def("db block gets from cache (fastpath)").order(10).xlat(buffers).minmax().register();
		//def("deferred (CURRENT) block cleanout applications").order(10).xlat(num).minmax().register();
		//def("enqueue conversions").order(10).xlat(num).minmax().register();
		//def("enqueue releases").order(10).xlat(num).minmax().register();
		//def("enqueue requests").order(10).xlat(num).minmax().register();
		//def("enqueue timeouts").order(10).xlat(num).minmax().register();
		//def("enqueue waits").order(10).xlat(num).minmax().register();
		//def("execute count").order(10).xlat(num).minmax().register(); DUPLICATE
		//def("file io service time").order(10).xlat(num).minmax().register();
		//def("file io wait time").order(10).xlat(num).minmax().register();
		//def("free buffer requested").order(10).xlat(num).minmax().register();
		//def("heap block compress").order(10).xlat(num).minmax().register();
		//def("immediate (CR) block cleanout applications").order(10).xlat(num).minmax().register();
		//def("immediate (CURRENT) block cleanout applications").order(10).xlat(num).minmax().register();
		//def("in call idle wait time").order(10).xlat(num).minmax().register();
		//def("index crx upgrade (positioned)").order(10).xlat(num).minmax().register();
		def("index fast full scans (full)").order(10).xlat(num).minmax().register();
		def("index fetch by key").order(10).xlat(num).minmax().register();
		def("index scans kdiixs1").order(10).xlat(num).minmax().register();
		//def("leaf node 90-10 splits").order(10).xlat(num).minmax().register();
		//def("leaf node splits").order(10).xlat(num).minmax().register();
		def("lob reads").order(10).xlat(num).minmax().register();
		def("lob writes").order(10).xlat(num).minmax().register();
		def("lob writes unaligned").order(10).xlat(num).minmax().register();
		def("logical read bytes from cache").order(10).xlat(bytes).minmax().register();
		//def("logons cumulative").order(10).xlat(num).minmax().register();
		//def("logons current").order(10).xlat(num).minmax().register();
		//def("max cf enq hold time").order(10).xlat(num).minmax().register();
		def("messages received").order(10).xlat(num).minmax().register();
		def("messages sent").order(10).xlat(num).minmax().register();
		//def("min active SCN optimization applied on CR").order(10).xlat(num).minmax().register();
		def("no buffer to keep pinned count").order(10).xlat(num).minmax().register();
		def("no work - consistent read gets").order(10).xlat(num).minmax().register();
		def("non-idle wait count").order(10).xlat(num).minmax().register();
		def("non-idle wait time").order(10).xlat(tensofms).minmax().register();
		def("opened cursors cumulative").order(10).xlat(num).minmax().register();
		def("opened cursors current").order(10).xlat(num).minmax().register();
		def("parse count (failures)").order(10).xlat(num).minmax().register();
		def("parse count (hard)").order(10).xlat(num).minmax().register();
		//def("parse count (total)").order(10).xlat(num).minmax().register(); DUPLICATE
		def("parse time cpu").order(10).xlat(num).minmax().register();
		//def("parse time elapsed").order(10).xlat(num).minmax().register(); DUPLICATE
		def("physical read IO requests").order(10).xlat(num).minmax().register();
		def("physical read bytes").order(10).xlat(bytes).minmax().register();
		def("physical read total IO requests").order(10).xlat(num).minmax().register();
		def("physical read total bytes").order(10).xlat(bytes).minmax().register();
		def("physical read total multi block requests").order(10).xlat(num).minmax().register();
		//def("physical reads").order(10).xlat(buffers).minmax().register();
		def("physical reads cache").order(10).xlat(buffers).minmax().register();
		def("physical reads cache prefetch").order(10).xlat(buffers).minmax().register();
		def("physical reads direct").order(10).xlat(buffers).minmax().register();
		def("physical reads direct (lob)").order(10).xlat(buffers).minmax().register();
		def("physical reads direct temporary tablespace").order(10).xlat(buffers).minmax().register();
		def("physical reads prefetch warmup").order(10).xlat(num).minmax().register();
		def("physical write IO requests").order(10).xlat(num).minmax().register();
		def("physical write bytes").order(10).xlat(bytes).minmax().register();
		def("physical write total IO requests").order(10).xlat(num).minmax().register();
		def("physical write total bytes").order(10).xlat(bytes).minmax().register();
		def("physical write total multi block requests").order(10).xlat(num).minmax().register();
		def("physical writes").order(10).xlat(buffers).minmax().register();
		def("physical writes direct").order(10).xlat(buffers).minmax().register();
		def("physical writes direct (lob)").order(10).xlat(buffers).minmax().register();
		def("physical writes direct temporary tablespace").order(10).xlat(buffers).minmax().register();
		def("physical writes from cache").order(10).xlat(buffers).minmax().register();
		def("physical writes non checkpoint").order(10).xlat(buffers).minmax().register();
		//def("pinned cursors current").order(10).xlat(num).minmax().register();
		//def("process last non-idle time").order(10).xlat(num).minmax().register();
		//def("recursive calls").order(10).xlat(num).minmax().register();
		//def("recursive cpu usage").order(10).xlat(num).minmax().register();
		//def("redo blocks checksummed by FG (exclusive)").order(10).xlat(num).minmax().register();
		//def("redo blocks written").order(10).xlat(num).minmax().register();
		//def("redo entries").order(10).xlat(num).minmax().register();
		//def("redo ordering marks").order(10).xlat(num).minmax().register();
		//def("redo size").order(10).xlat(num).minmax().register();
		//def("redo size for direct writes").order(10).xlat(num).minmax().register();
		//def("redo subscn max counts").order(10).xlat(num).minmax().register();
		//def("redo synch long waits").order(10).xlat(num).minmax().register();
		//def("redo synch time").order(10).xlat(num).minmax().register();
		//def("redo synch time (usec)").order(10).xlat(num).minmax().register();
		//def("redo synch time overhead (usec)").order(10).xlat(num).minmax().register();
		//def("redo synch time overhead count (<128 msec)").order(10).xlat(num).minmax().register();
		//def("redo synch time overhead count (<2 msec)").order(10).xlat(num).minmax().register();
		//def("redo synch time overhead count (<32 msec)").order(10).xlat(num).minmax().register();
		//def("redo synch time overhead count (<8 msec)").order(10).xlat(num).minmax().register();
		//def("redo synch writes").order(10).xlat(num).minmax().register();
		//def("redo wastage").order(10).xlat(num).minmax().register();
		//def("redo write info find").order(10).xlat(num).minmax().register();
		//def("redo write time").order(10).xlat(num).minmax().register();
		//def("redo writes").order(10).xlat(num).minmax().register();
		//def("rollback changes - undo records applied").order(10).xlat(num).minmax().register();
		//def("rollbacks only - consistent read gets").order(10).xlat(num).minmax().register();
		//def("rows fetched via callback").order(10).xlat(num).minmax().register();
		//def("session connect time").order(10).xlat(num).minmax().register();
		//def("session cursor cache count").order(10).xlat(num).minmax().register();
		//def("session cursor cache hits").order(10).xlat(num).minmax().register();
		//def("session logical reads").order(10).xlat(buffers).minmax().register();
		//def("session pga memory").order(10).xlat(num).minmax().register();
		//def("session pga memory max").order(10).xlat(num).minmax().register();
		//def("session uga memory").order(10).xlat(num).minmax().register();
		//def("session uga memory max").order(10).xlat(num).minmax().register();
		//def("shared hash latch upgrades - no wait").order(10).xlat(num).minmax().register();
		//def("shared hash latch upgrades - wait").order(10).xlat(num).minmax().register();
		def("sorts (memory)").order(10).xlat(num).minmax().register();
		def("sorts (rows)").order(10).xlat(num).minmax().register();
		//def("sql area evicted").order(10).xlat(num).minmax().register();
		//def("sql area purged").order(10).xlat(num).minmax().register();
		//def("switch current to new buffer").order(10).xlat(num).minmax().register();
		def("table fetch by rowid").order(10).xlat(num).minmax().register();
		def("table fetch continued row").order(10).xlat(num).minmax().register();
		def("table scan blocks gotten").order(10).xlat(buffers).minmax().register();
		def("table scan rows gotten").order(10).xlat(num).minmax().register();
		def("table scans (direct read)").order(10).xlat(num).minmax().register();
		def("table scans (long tables)").order(10).xlat(num).minmax().register();
		def("table scans (short tables)").order(10).xlat(num).minmax().register();
		//def("total cf enq hold time").order(10).xlat(num).minmax().register();
		//def("total number of cf enq holders").order(10).xlat(num).minmax().register();
		//def("total number of times SMON posted").order(10).xlat(num).minmax().register();
		def("transaction rollbacks").order(10).xlat(num).minmax().register();
		//def("undo change vector size").order(10).xlat(num).minmax().register();
		//def("user I/O wait time").order(10).xlat(num).minmax().register();		DUPLICATE
		//def("user calls").order(10).xlat(num).minmax().register();
		//def("user commits").order(10).xlat(num).minmax().register();
		//def("user logons cumulative").order(10).xlat(num).minmax().register();
		//def("user rollbacks").order(10).xlat(num).minmax().register();
		//def("workarea executions - onepass").order(10).xlat(num).minmax().register();
		//def("workarea executions - optimal").order(10).xlat(num).minmax().register();
	}

	static public MetricBuilder def(String key) {
		return new MetricBuilder(key);
	}


	public static List<String> metricNamesByID(int id) {
		List<String> res = new ArrayList<>();
		for(MetricsDefinition md: m_definitionMap.values()) {
			if(md.getId() == id) {
				res.add(md.getKey());
			}
		}
		return res;
	}

	static public synchronized MetricsDefinition getOrCreate(@Nonnull String name) {
		MetricsDefinition md = m_definitionMap.get(name);
		if(null == md) {
			md = new MetricsDefinition(-1, name, name, a -> String.valueOf(a), null, false, 0, null);
			m_definitionMap.put(name, md);
		}
		return md;
	}

	static public DbMetric createByName(String metricName, double value) {
		MetricsDefinition md = getOrCreate(metricName);
		return new DbMetric(md, value);
	}

	public int getOrder() {
		return m_order;
	}

	public boolean isDefined() {
		return m_defined;
	}

	public int[] getMinmax() {
		return m_minmax;
	}

	@Override
	public String toString() {
		return m_key;
	}

	//public static TranslatedMetric translate(DbMetric metric) {
	//	MetricsDefinition d = get(metric.getName());
	//	if(null == d) {
	//		return new TranslatedMetric(metric.getName(), metric.getName(), metric.getName(), Double.toString(metric.getValue()));
	//	}
	//
	//	return new TranslatedMetric(d.getKey(), d.getLabel(), d.getDescription(), d.getTranslator().translate(metric.getValue()));
	//}
}
