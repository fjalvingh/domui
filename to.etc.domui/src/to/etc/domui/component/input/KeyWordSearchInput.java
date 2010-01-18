package to.etc.domui.component.input;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class KeyWordSearchInput extends Div {

	private int m_resultsCount = -1; //-1 states for not visible

	private TextStr m_keySearch;

	private Label m_lblSearchCount;

	private BR m_newLine;

	private IValueChanged<KeyWordSearchInput> m_onTyping;

	private IValueChanged<KeyWordSearchInput> m_onShowResults;

	public KeyWordSearchInput() {
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		m_keySearch = new TextStr();

		m_keySearch.setOnTyping(new ITypingListener<TextStr>() {

			@Override
			public void onTyping(TextStr component, boolean done) throws Exception {
				if(done) {
					if(getOnShowResults() != null) {
						getOnShowResults().onValueChanged(KeyWordSearchInput.this);
					}
				} else {
					if(getOnTyping() != null) {
						getOnTyping().onValueChanged(KeyWordSearchInput.this);
					}
				}
			}
		});

		add(m_keySearch);
		renderResultsCountPart();
	}

	public IValueChanged<KeyWordSearchInput> getOnTyping() {
		return m_onTyping;
	}

	public void setOnTyping(IValueChanged<KeyWordSearchInput> onTyping) {
		m_onTyping = onTyping;
	}

	public String getKeySearchValue() {
		return m_keySearch.getValue();
	}

	/**
	 * Set number of results label. Use -1 for hidding label.
	 * @param results
	 */
	public void setResultsCount(int results) {
		if(results != m_resultsCount) {
			m_resultsCount = results;
			if(isBuilt()) {
				renderResultsCountPart();
			}
		}
	}

	private void renderResultsCountPart() {
		if(m_resultsCount == -1 || m_resultsCount == 1) {
			if(m_lblSearchCount != null) {
				removeChild(m_lblSearchCount);
			}
			m_lblSearchCount = null;
			if(m_newLine != null) {
				removeChild(m_newLine);
			}
			m_newLine = null;
		} else {
			if(m_newLine == null) {
				m_newLine = new BR();
				add(m_newLine);
			}
			if(m_lblSearchCount == null) {
				m_lblSearchCount = new Label();
				add(m_lblSearchCount);
			}
			//m_resultsCount + " record(s)"
			m_lblSearchCount.setText(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_COUNT, "" + m_resultsCount));
		}
	}

	public IValueChanged<KeyWordSearchInput> getOnShowResults() {
		return m_onShowResults;
	}

	public void setOnShowResults(IValueChanged<KeyWordSearchInput> onShowResults) {
		m_onShowResults = onShowResults;
	}

}
