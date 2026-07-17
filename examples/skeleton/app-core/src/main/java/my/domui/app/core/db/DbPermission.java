package my.domui.app.core.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "au_group_permission")
public class DbPermission extends AbstractDbEntity {
    private DbGroup m_group;

    private String m_name;

    public static final String pGROUP = "group";

    public static final String pID = "id";

    public static final String pNAME = "name";

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "groupid")
    public DbGroup getGroup() {
        return m_group;
    }

    public void setGroup(DbGroup value) {
        m_group = value;
    }

    @Column(name = "right_name", length = 64, nullable = false)
    public String getName() {
        return m_name;
    }

    public void setName(String value) {
        m_name = value;
    }
}
