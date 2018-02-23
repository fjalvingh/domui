package my.domui.app.core.db;

import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;
import to.etc.domui.component.meta.MetaSearchItem;
import to.etc.domui.component.meta.SearchPropertyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "au_group")
@MetaObject(defaultColumns = { //
    @MetaDisplayProperty(name = DbGroup.pNAME, displayLength = 25) //
    , @MetaDisplayProperty(name = DbGroup.pDESCRIPTION, displayLength = 100) //
},
searchProperties = { //
    @MetaSearchItem(name = DbGroup.pNAME, order = 1, searchType = SearchPropertyType.BOTH)
    , @MetaSearchItem(name = DbGroup.pDESCRIPTION, order = 2, searchType = SearchPropertyType.BOTH)
    }
)
public class DbGroup extends AbstractDbEntity {
    private String m_name;

    private String m_description;

    private List<DbGroupMember> m_groupMemberList = new ArrayList<>();

    private List<DbPermission> m_permissionList = new ArrayList<>();

    public static final String pNAME = "name";

    public static final String pAUTHMEMBERSHIPLIST = "authMembershipList";

    public static final String pAUTHPERMISSIONLIST = "authPermissionList";

    public static final String pDESCRIPTION = "description";

    public static final String pID = "id";


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    public List<DbGroupMember> getGroupMemberList() {
        return m_groupMemberList;
    }

    public void setGroupMemberList(List<DbGroupMember> value) {
        m_groupMemberList = value;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    public List<DbPermission> getPermissionList() {
        return m_permissionList;
    }

    public void setPermissionList(List<DbPermission> value) {
        m_permissionList = value;
    }

    @Column(name = "description", length = 1024, nullable = true)
    public String getDescription() {
        return m_description;
    }

    public void setDescription(String value) {
        m_description = value;
    }

    @Column(name = "name", length = 64, nullable = false)
    public String getName() {
        return m_name;
    }

    public void setName(String value) {
        m_name = value;
    }
}
