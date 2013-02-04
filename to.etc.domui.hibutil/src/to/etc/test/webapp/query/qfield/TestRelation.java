package to.etc.test.webapp.query.qfield;

import java.util.*;

import javax.persistence.*;

/**
 *
 * Test class for generating QField classes
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
@Entity

public class TestRelation {


	@Column(name = "psn_propername", length = 40, nullable = true)
	public String getProperName() {
		return null;
	}

	@Column(name = "", length = 40, nullable = true)
	public double getAnum() {
		return 0;
	}

	/**
	 * Column FBT_ID_PREFERRED NUMBER(16, 0) NULL: De aangewezen bankrekening.
	 */
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "FBT_ID_PREFERRED")
	public TestBankAccount getPreferredAccount() {
		return null;
	}

	/**
	 * @see to.etc.test.webapp.query.qfield.TestRelation#getBankAccounts()
	 */

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = TestBankAccount.pRELATION)
	public List<TestBank> getBanks() {
		return null;
	}


	public
	Long getId() {
		return null;
	}


	public String getName() {
		return null;
	}



}
