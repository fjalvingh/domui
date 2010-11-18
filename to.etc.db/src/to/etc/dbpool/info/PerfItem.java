package to.etc.dbpool.info;

import javax.annotation.*;

/**
 * This encapsulates a single stored performance metric of a given type and key.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 8, 2010
 */
public class PerfItem {
	/** The key to use to prevent duplicates in the list, like screen name or sql statement. */
	@Nonnull
	final private String m_key;

	/** If applicable, an ident for the request that caused this metric. This is usually the complete URL for web requests. */
	@Nullable
	final private String m_request;

	/** Something which holds whatever data required to display this-item's metric data */
	@Nullable
	final private Object m_data;

	/** The actual value used to sort this in the top-xxx list. */
	final private long m_metric;

	public PerfItem(@Nonnull String key, long metric, @Nullable String request, @Nullable Object data) {
		m_key = key;
		m_metric = metric;
		m_data = data;
		m_request = request;
	}

	@Nonnull
	public String getKey() {
		return m_key;
	}

	@Nullable
	public Object getData() {
		return m_data;
	}

	public long getMetric() {
		return m_metric;
	}

	@Nullable
	public String getRequest() {
		return m_request;
	}
}
