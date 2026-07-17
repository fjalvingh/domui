package my.domui.app.core.db;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "au_group_member")
public class DbGroupMember extends AbstractDbEntity {
    private DbGroup m_group;

    private DbUser m_user;

    public static final String pGROUP = "group";

    public static final String pID = "id";

    public static final String pUSER = "user";

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "groupid")
    public DbGroup getGroup() {
        return m_group;
    }

    public void setGroup(DbGroup value) {
        m_group = value;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "userid")
    public DbUser getUser() {
        return m_user;
    }

    public void setUser(DbUser value) {
        m_user = value;
    }
}
