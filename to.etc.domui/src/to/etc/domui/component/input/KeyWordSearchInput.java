package to.etc.domui.component.input;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Represents keyword search panel that is used from other components, like LookupInput.
 * Shows input field, marker that shows found results and waiting image that is hidden by default.
 * Waiting image is passive here, but it is used from browser script.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 21 Jan 2010
 */
class KeyWordSearchInput extends Div {

	private int m_resultsCount = -1; //-1 states for not visible

	private TextStr m_keySearch = new TextStr();

	private Div m_pnlSearchCount;

	private IValueChanged<KeyWordSearchInput> m_onTyping;

	private IValueChanged<KeyWordSearchInput> m_onShowResults;

	private Img m_imgWaiting;

	public KeyWordSearchInput() {
		super();
	}

	public KeyWordSearchInput(String m_inputCssClass) {
		super();
		m_keySearch.setCssClass(m_inputCssClass);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		//position must be set to relative to enable absoulute positioning of child elements (waiting image)
		setPosition(PositionType.RELATIVE);
		m_imgWaiting = new Img("THEME/wait16trans.gif");
		m_imgWaiting.setCssClass("ui-lui-waiting");
		m_imgWaiting.setDisplay(DisplayType.NONE);
		if(m_keySearch.getCssClass() == null) {
			m_keySearch.setCssClass("ui-lui-keyword");
		}
		m_keySearch.setMaxLength(40);
		m_keySearch.setSize(14);

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

		add(m_imgWaiting);
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
			if(m_pnlSearchCount != null) {
				removeChild(m_pnlSearchCount);
			}
			m_pnlSearchCount = null;
		} else {
			if(m_pnlSearchCount == null) {
				m_pnlSearchCount = new Div();
				add(m_pnlSearchCount);
			}
			if(m_resultsCount == 0) {
				m_pnlSearchCount.setCssClass("ui-lui-keyword-no-res");
				m_pnlSearchCount.setText(Msgs.BUNDLE.getString(Msgs.UI_KEYWORD_SEARCH_NO_MATCH));
			} else if(m_resultsCount == ITableModel.MAX_SIZE) {
				//usually this means that query cutoff rest data, corner case when real m_resultsCount == MAX_RESULTS is not that important  
				m_pnlSearchCount.setCssClass("ui-lui-keyword-large");
				m_pnlSearchCount.setText(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_LARGE_MATCH, "" + ITableModel.MAX_SIZE));
			} else {
				if(m_resultsCount > ITableModel.MAX_SIZE) {
					//in case that query does not cutoff rest of data (JDBC queries) report actual data size, but render as to large match
					m_pnlSearchCount.setCssClass("ui-lui-keyword-large");
				} else {
					m_pnlSearchCount.setCssClass("ui-lui-keyword-res");
				}
				m_pnlSearchCount.setText(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_COUNT, "" + m_resultsCount));
			}
		}
	}

	public IValueChanged<KeyWordSearchInput> getOnShowResults() {
		return m_onShowResults;
	}

	public void setOnShowResults(IValueChanged<KeyWordSearchInput> onShowResults) {
		m_onShowResults = onShowResults;
	}

	@Override
	public void setFocus() {
		if(m_keySearch != null) {
			m_keySearch.setFocus();
		}
	}

}
