package to.etc.domui.component.lookup;

import javax.annotation.*;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/28/16.
 */
@DefaultNonNull
public final class SavedFilter {
	private final Long m_recordId;
	private final String m_filterName;
	private final String m_filterValue;

	public SavedFilter(Long recordId, String filterName, String filterValue) {
		m_recordId = recordId;
		m_filterName = filterName;
		m_filterValue = filterValue;
	}

	/**
	 * This is the name the user assigns when saving a search filter
	 * @return
	 */
	public String getFilterName() {
		return m_filterName;
	}

	/**
	 * The serialized string that contains in XML format the serialized filter
	 * @return
	 */
	public String getFilterValue() {
		return m_filterValue;
	}

	/**
	 * The id in the database that this filter has
	 * @return
	 */
	public Long getRecordId() {
		return m_recordId;
	}
}
