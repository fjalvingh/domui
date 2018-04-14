package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;

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

	private final @NonNull
	String m_code;

	BrowserModel(@NonNull String code) {
		m_code = code;
	}

	@NonNull
	String getCode() {
		return m_code;
	}

	public static BrowserModel get(@NonNull String browserString) {
		for(BrowserModel kind : values()) {
			if(kind.getCode().equalsIgnoreCase(browserString)) {
				return kind;
			}
		}
		throw new IllegalStateException("Not supported browser string! [" + browserString + "]");
	}
}
