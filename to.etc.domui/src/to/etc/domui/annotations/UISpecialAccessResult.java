package to.etc.domui.annotations;

/**
 * Represents result of special access check method. 
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 20 Dec 2011
 */
public class UISpecialAccessResult {
	public enum Status {
		/** Unchanged - access check would pass to standard rights check */
		NONE, //
		/** Forced accept - access check bypass standard rights check - user can access without required rights */
		ACCEPT, //
		/** Forced refusal - access check bypass standard rights check - user can't access resource even when it has required rights. This result usually comes in pair with some refusal reason message. */
		REFUSE
	};

	private final String m_refuseReason;

	private final Status m_status;

	/**
	 * Returns refusal reason.
	 * @return
	 */
	public String getRefuseReason() {
		return m_refuseReason;
	}

	/**
	 * Returns check result status. 
	 * @return
	 */
	public Status getStatus() {
		return m_status;
	}

	public UISpecialAccessResult(String refuseReason, Status status) {
		m_refuseReason = refuseReason;
		m_status = status;
	}

	/**
	 * Use to indicate that no special access check action should be applied. 
	 */
	public static final UISpecialAccessResult DEFAULT = new UISpecialAccessResult(null, Status.NONE);

	/**
	 * Use to indicate that forced access should be applied. 
	 */
	public static final UISpecialAccessResult ACCEPTED = new UISpecialAccessResult(null, Status.ACCEPT);
}
