package my.domui.app.core.authentication;

import my.domui.app.core.db.DbGroup;
import my.domui.app.core.db.DbGroupMember;
import my.domui.app.core.db.DbPermission;
import my.domui.app.core.db.DbUser;
import to.etc.domui.login.IUser;
import to.etc.domui.state.UIContext;
import to.etc.webapp.query.QDataContext;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A context representing the logged-in user. Maintained in the
 * session context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@DefaultNonNull
public class LoginUser implements IUser {
	@Nonnull
	private final String m_loginName;

	@Nonnull
	final private DbUser m_user;

	/** Set when user has ADMIN right so all other checks are unneeded. */
	private boolean m_admin;

	private final Set<DbGroup> m_groupSet;

	private final Set<String> m_permissionNameSet;

	private final Set<DbPermission> m_permissionSet;

	public LoginUser(QDataContext dc, @Nonnull final DbUser p) {
		m_user = p;
		m_loginName = p.getEmail();

		//-- Load all groups
		Set<String> groupNameSet = new HashSet<>();
		Set<String> permissionNameSet = new HashSet<>();
		Set<DbGroup> groupSet = new HashSet<>();
		Set<DbPermission> permSet = new HashSet<>();

		for(DbGroupMember membership : p.getGroupMemberList()) {
			DbGroup authGroup = membership.getGroup();
			groupSet.add(authGroup);
			groupNameSet.add(authGroup.getName());

			for(DbPermission permission : authGroup.getPermissionList()) {
				permSet.add(permission);
				permissionNameSet.add(permission.getName());
			}
		}

		m_permissionNameSet = Collections.unmodifiableSet(permissionNameSet);
		m_permissionSet = Collections.unmodifiableSet(permSet);
		m_groupSet = Collections.unmodifiableSet(groupSet);
	}

	@Nonnull
	static public LoginUser create(@Nonnull QDataContext dc, @Nonnull DbUser whom) throws Exception {
		return new LoginUser(dc, whom);
	}

	@Nullable
	static public LoginUser	findCurrent() {
		return (LoginUser) UIContext.getCurrentUser();
	}

	@Nullable
	static public DbUser findCurrentUser() {
		LoginUser	lu = findCurrent();
		return lu == null ? null : lu.getUser();
	}

	@Nonnull
	static public LoginUser	getCurrent() {
		return (LoginUser) UIContext.getLoggedInUser();
	}

	@Nonnull
	static public DbUser getCurrentUser() {
		LoginUser lu = getCurrent();
		return lu.getUser();
	}

	@Override
	public String getLoginID() {
		return m_loginName;
	}

	@Nonnull
	@Override
	public String getDisplayName() {
		DbUser user = m_user;
		return user == null ? "UNKNOWN" : user.getFullName();
	}

	public DbUser getUser() {
		return m_user;
	}

	/**
	 * Returns T if this user has this right assigned.
	 *
	 * @see to.etc.domui.login.IUser#hasRight(String)
	 */
	@Override
	public boolean hasRight(@Nonnull String r) {
		return m_permissionNameSet.contains(r) || m_permissionNameSet.contains(Rights.ADMIN);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.login.IUser#hasRight(String, Object)
	 */
	@Override
	public <T> boolean hasRight(@Nonnull String r, @Nullable T dataElement) {
		return true;
	}

	public Set<DbGroup> getGroupSet() {
		return m_groupSet;
	}

	public Set<String> getPermissionNameSet() {
		return m_permissionNameSet;
	}

	public Set<DbPermission> getPermissionSet() {
		return m_permissionSet;
	}

	@Override public boolean canImpersonate() {
		return hasRight(Rights.ADMIN);
	}
}
