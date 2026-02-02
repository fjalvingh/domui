package to.etc.testutil;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class TestCondition {
	private final TestConditionSet m_scenario;

	private boolean m_ordered;

	private final String m_name;

	private boolean m_resolved;

	@Nullable
	private Exception m_exception;

	TestCondition(TestConditionSet scenario, boolean ordered, String name) {
		m_scenario = scenario;
		m_ordered = ordered;
		m_name = name;
	}

	public boolean isResolved() {
		synchronized(m_scenario) {
			return m_resolved;
		}
	}

	public boolean isOrdered() {
		return m_ordered;
	}

	@Nullable
	public Exception getException() {
		synchronized(m_scenario) {
			return m_exception;
		}
	}

	public void resolved() {
		synchronized(m_scenario) {
			if(m_ordered && ! m_scenario.areOrderedConditionsBeforeThisResolved(this)) {
				System.out.println("testCondition: trying to set ordered condition to resolved but its predecessors are not yet resolved");
				return;
			}

			if(m_exception != null)							// Already failed before
				return;
			m_resolved = true;
			m_scenario.notifyAll();
		}
	}

	public void failed(Exception e) {
		synchronized(m_scenario) {
			m_exception = e;
			m_scenario.notifyAll();
		}
	}

	public void failed(String why) {
		failed(new TestConditionFailedException(why));
	}

	public String getName() {
		return m_name;
	}
}

