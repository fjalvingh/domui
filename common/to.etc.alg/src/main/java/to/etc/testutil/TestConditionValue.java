package to.etc.testutil;

import java.util.function.Predicate;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 04-01-21.
 */
public class TestConditionValue<T> extends TestCondition {
	private final Class<T> m_dataType;

	private final Predicate<T> m_predicate;

	public TestConditionValue(TestConditionSet scenario, boolean ordered, String name, Class<T> dataType, Predicate<T> predicate) {
		super(scenario, ordered, name);
		m_predicate = predicate;
		m_dataType = dataType;
	}

	void checkValue(Object value) {
		if(value == null)
			return;
		if(m_dataType.isAssignableFrom(value.getClass())) {
			if(m_predicate.test((T) value)) {
				//System.out.println("conditionvalue: " + getName() + " resolved on receiving " + value);
				resolved();
			}
		}
	}
}
