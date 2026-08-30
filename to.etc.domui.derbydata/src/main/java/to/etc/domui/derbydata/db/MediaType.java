package to.etc.domui.derbydata.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import to.etc.domui.component.meta.MetaCombo;
import to.etc.domui.component.meta.MetaComboProperty;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;

@Entity
@Table(name = "MediaType")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")}, defaultSortColumn = "name")
@MetaCombo(properties = @MetaComboProperty(name = "name"))
public class MediaType extends DbRecordBase<Long> {
	private Long m_id;

	private String m_name;

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "track_sq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq")
	@Column(name = "MediaTypeId", precision = 20, nullable = false)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(name = "Name", length = 120, nullable = false)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
}
