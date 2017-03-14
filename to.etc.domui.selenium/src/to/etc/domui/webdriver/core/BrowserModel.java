package to.etc.domui.webdriver.core;

import javax.annotation.Nonnull;

public enum BrowserModel {
	FIREFOX("firefox"), CHROME("chrome"), IE("ie"), IE9("ie9"), IE10("ie10"), IE11("ie11"), EDGE("edge");

	private final @Nonnull
	String m_code;

	BrowserModel(@Nonnull String code) {
		m_code = code;
	}

	@Nonnull
	String getCode() {
		return m_code;
	}

	public static BrowserModel get(@Nonnull String browserString) {
		for(BrowserModel kind : values()) {
			if(kind.getCode().equalsIgnoreCase(browserString)) {
				return kind;
			}
		}
		throw new IllegalStateException("Not supported browser string! [" + browserString + "]");
	}
}
