package my.domui.app.core.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
