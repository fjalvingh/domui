package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.webapp.query.QOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents the numeric value to lookup.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
final public class NumberLookupValue {
	@Nullable
	private final Number m_from;

	@Nonnull
	private final QOperation m_fromOperation;

	@Nullable
	private final Number m_to;

	@Nullable
	private final QOperation m_toOperation;

	@Nullable
	private String m_likeString;

	public NumberLookupValue(String like) {
		m_fromOperation = QOperation.LIKE;
		m_likeString = like;
		m_to = null;
		m_toOperation = null;
		m_from = null;
	}

	public NumberLookupValue(@Nonnull QOperation fromOperation, @Nullable Number from) {
		this(fromOperation, from, null, null);
	}

	public NumberLookupValue(@Nonnull QOperation fromOperation, @Nullable Number from, QOperation toOperation, Number to) {
		m_from = from;
		m_fromOperation = fromOperation;
		m_to = to;
		m_toOperation = toOperation;
	}

	@Nullable public Number getFrom() {
		return m_from;
	}

	@Nonnull public QOperation getFromOperation() {
		return m_fromOperation;
	}

	@Nullable public Number getTo() {
		return m_to;
	}

	@Nullable public QOperation getToOperation() {
		return m_toOperation;
	}

	@Override public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		NumberLookupValue that = (NumberLookupValue) o;
		return Objects.equals(m_from, that.m_from) &&
			m_fromOperation == that.m_fromOperation &&
			Objects.equals(m_to, that.m_to) &&
			m_toOperation == that.m_toOperation;
	}

	@Override public int hashCode() {
		return Objects.hash(m_from, m_fromOperation, m_to, m_toOperation);
	}
}
