package to.etc.testutil;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@NonNullByDefault
final public class TestConditionSet {
	private final List<TestCondition> m_testConditionList = new ArrayList<>();

	public TestConditionSet() {
	}

	public void await(Duration timeout) throws Exception {
		var endTime = System.currentTimeMillis() + timeout.toMillis();
		synchronized(this) {
			while(true) {
				var unresolved = new ArrayList<TestCondition>();
				for(TestCondition testCondition : m_testConditionList) {
					Exception exception = testCondition.getException();
					if(null != exception)
						throw exception;
					if(!testCondition.isResolved())
						unresolved.add(testCondition);
				}
				if(unresolved.isEmpty()) {
					return;
				}
				var currentTime = System.currentTimeMillis();
				var remaining = endTime - currentTime;
				if(remaining <= 0) {
					throw new TestConditionTimeoutException(timeout, unresolved);
				}
				wait(remaining);
			}
		}
	}

	boolean areOrderedConditionsBeforeThisResolved(TestCondition condition) {
		synchronized(this) {
			for(TestCondition testCondition : m_testConditionList) {
				if(testCondition == condition)								// If all were OK before this one we're ok.
					return true;
				if(testCondition.isOrdered()) {
					if(!testCondition.isResolved())
						return false;
				}
			}
		}

		//-- We did not find the condition specified -> something's terribly wrong..
		throw new IllegalStateException("The condition passed (" + condition + ") was not found in the condition list");
	}

	/**
	 * Create a normal, unordered condition.
	 */
	public TestCondition createCondition(String name) {
		TestCondition c = new TestCondition(this, false, name);
		synchronized(this) {
			m_testConditionList.add(c);
		}
		return c;
	}

	public void gotValue(Object v) {
		synchronized(this) {
			for(TestCondition testCondition : m_testConditionList) {
				if(testCondition instanceof TestConditionValue) {
					TestConditionValue<Object> real = (TestConditionValue<Object>) (TestConditionValue<?>) testCondition;
					real.checkValue(v);
				}
			}
		}
	}

	/**
	 * Create an ordered condition: this one can only become true when all other ordered
	 * conditions before it have become true.
	 */
	public TestCondition createOrderedCondition(String name) {
		TestCondition c = new TestCondition(this, true, name);
		synchronized(this) {
			m_testConditionList.add(c);
		}
		return c;
	}

	public <T> TestConditionValue<T> createOrderedConditionValue(String name, Class<T> dataClass, Predicate<T> test) {
		TestConditionValue<T> cv = new TestConditionValue<>(this, true, name, dataClass, test);
		synchronized(this) {
			m_testConditionList.add(cv);
		}
		return cv;
	}

	public <T> TestConditionValue<T> createOrderedConditionValue(String name, T instance) {
		TestConditionValue<T> cv = new TestConditionValue<T>(this, true, name, (Class<T>) instance.getClass(), a -> a == instance);
		synchronized(this) {
			m_testConditionList.add(cv);
		}
		return cv;
	}

	public <T> TestConditionValue<T> createOrderedConditionValue(T instance) {
		TestConditionValue<T> cv = new TestConditionValue<T>(this, true, instance.getClass().getSimpleName() + "#" + instance.toString(), (Class<T>) instance.getClass(), a -> a == instance);
		synchronized(this) {
			m_testConditionList.add(cv);
		}
		return cv;
	}


}
