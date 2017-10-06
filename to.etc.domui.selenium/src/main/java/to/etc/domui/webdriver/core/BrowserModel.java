package to.etc.domui.webdriver.core;

import javax.annotation.Nonnull;

public enum BrowserModel {
	FIREFOX("firefox")
	, CHROME("chrome-desktop")
	, CHROME_HEADLESS("chrome")
	, IE("ie")
	, IE9("ie9")
	, IE10("ie10")
	, IE11("ie11")
	, EDGE14("edge-14")
	, EDGE15("edge-15")
	, PHANTOMJS("phantomjs");

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
