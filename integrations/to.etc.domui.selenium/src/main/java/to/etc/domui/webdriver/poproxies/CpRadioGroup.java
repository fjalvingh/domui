package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 03-02-22.
 */
public class CpRadioGroup<T> extends AbstractCpComponent {
	final private List<Pair<T, String>> m_valueSelectorList;

	public CpRadioGroup(WebDriverConnector wd, Supplier<String> selectorProvider, Object... valueSelectorList) {
		super(wd, selectorProvider);
		if(valueSelectorList.length == 0)
			throw new IllegalStateException("No members in the value/selector list");
		if(valueSelectorList.length % 2 == 1)
			throw new IllegalStateException("Odd number of entries in the value/selector list");

		m_valueSelectorList = new ArrayList<>();
		for(int i = 0; i < valueSelectorList.length; i += 2) {
			T value = (T) valueSelectorList[i];
			String selector = (String) valueSelectorList[i + 1];
			m_valueSelectorList.add(new Pair<>(value, selector));
		}
	}

	/**
	 * Get the selected value for the thing.
	 */
	@Nullable
	public T getValue() {
		for(Pair<T, String> pair : m_valueSelectorList) {
			WebElement button = findRadioButton(pair.get2());
			if(button.isSelected()) {
				return pair.get1();
			}
		}
		return null;
	}

	public void setValue(@Nullable T value) {
		if(null == value)
			return;

		//-- Find the thingy to click
		for(Pair<T, String> pair : m_valueSelectorList) {
			if(Objects.equals(pair.get1(), value)) {
				WebElement button = findRadioButton(pair.get2());
				clickButton(button);
				T newVa = getValue();
				if(! Objects.equals(newVa, value))
					throw new IllegalStateException("Current value of radiogroup is not the same as set (value=" + newVa + ", set was " + value + ")");
				return;
			}

		}
		throw new IllegalStateException("Unknown radiobutton for value=" + value);
	}

	private void clickButton(WebElement element) {
		//-- If this is display=none we need the label instead
		try {
			element.click();
			return;
		} catch(ElementNotInteractableException ina) {
			// ignore
		}

		//-- Find the accompanying label
		String elementId = element.getAttribute("id");
		By labelSelector = By.cssSelector("label[for='" + elementId + "']");
		WebElement label = wd().getElement(labelSelector);
		label.click();
	}


	private WebElement findRadioButton(String subSelector) {
		By selector = selector(" " + subSelector);
		WebElement element = wd().findElement(selector);
		if(null == element)
			return null;

		//System.out.println("rb: " + element.getTagName() + " id=" + element.getAttribute("id"));
		return element;
	}
}
