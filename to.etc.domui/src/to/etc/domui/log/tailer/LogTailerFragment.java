package to.etc.domui.log.tailer;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.delayed.*;
import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;

/**
 * This fragment shows lines from a tailed file as they come in.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 2, 2012
 */
public class LogTailerFragment extends PollingDiv {
	/** The path of the tailed file on that server. */
	@Nonnull
	final private String m_logpath;

	private LogTailerTask m_task;

	/** The div containing the actual lines to show. */
	final private Div m_lineDiv = new Div();

	private long m_oldSize = -1;

	private int m_oldLines = -999;

	/** The current start line shown on the page. */
	private int m_startLine;

	private Checkbox m_followBox;

	private Text<Integer> m_goto;

	private Span m_lineSpan;

	private Span m_szSpan;

	private ComboFixed<Integer> m_linesCombo;

	private boolean m_noFile;

	//	private Div m_debugDiv;


	public LogTailerFragment(String logpath) {
		m_logpath = logpath;
	}

	private boolean createTask() throws Exception {
		if(m_task != null) {
			throw new IllegalStateException("task already created!");
		}
		m_task = new LogTailerTask(m_logpath);
		getPage().getConversation().setAttribute("xx", m_task);
		return m_task.start();
	}

	@Override
	public void createContent() throws Exception {
		if (!createTask()){
			m_noFile = true;
			showNoFileError();
			return;
		}
		createHeader();
		add(m_lineDiv);
		//		m_debugDiv = new Div();
		//		add(m_debugDiv);

		updateSizes();
		updateLines(0);
	}

	private void showNoFileError() {
		add("Log file does not exists yet: " + m_logpath);
		add(new BR());
		add("Go back and try to follow log later.");
	}

	static private int[] SIZES = {32, 50, 60, 70, 80, 90, 100, 110, 120};

	/**
	 * Shows the log header line: something like:
	 * [ (&lt;prev) (&gt;next) [x]follow Goto [1231221] |  38,337 lines, 2.6 MB | cat:/home/puzzler/stdout-2012-09-03.log ]
	 */
	private void createHeader() {
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("ui-tlf-hdr");

		//-- 1. Buttons.
		Div btn = new Div();
		ttl.add(btn);
		btn.setCssClass("ui-tlf-btn");
		SmallImgButton ib = new SmallImgButton("img/btnFirst.png", new IClicked<SmallImgButton>() {
			@Override
			public void clicked(@Nonnull SmallImgButton clickednode) throws Exception {
				gotoLine(0);
			}
		});
		btn.add(ib);

		ib = new SmallImgButton("img/btnPrev.png", new IClicked<SmallImgButton>() {
			@Override
			public void clicked(@Nonnull SmallImgButton clickednode) throws Exception {
				int lnr = m_startLine - getLinesPerPage();
				if(lnr < 0)
					lnr = 0;
				gotoLine(lnr);
			}
		});
		btn.add(ib);

		ib = new SmallImgButton("img/btnNext.png", new IClicked<SmallImgButton>() {
			@Override
			public void clicked(@Nonnull SmallImgButton clickednode) throws Exception {
				int last = m_task.getLastLine();
				int lnr = m_startLine + getLinesPerPage();
				if(lnr > last)
					lnr = last - getLinesPerPage();
				gotoLine(lnr);
			}
		});
		btn.add(ib);

		ib = new SmallImgButton("img/btnLast.png", new IClicked<SmallImgButton>() {
			@Override
			public void clicked(@Nonnull SmallImgButton clickednode) throws Exception {
				gotoLine(m_task.getLastLine() - getLinesPerPage());
			}
		});
		btn.add(ib);

		btn.add(" Goto ");
		m_goto = new Text<Integer>(Integer.class);
		btn.add(m_goto);
		m_goto.setMaxLength(10);
		m_goto.setSize(6);
		m_goto.setOnValueChanged(new IValueChanged<Text<Integer>>() {
			@Override
			public void onValueChanged(@Nonnull Text<Integer> component) throws Exception {
				Integer value = component.getValue();
				if(value != null)
					gotoLine(value.intValue());
			}
		});

		btn.add(" ");
		m_followBox = new Checkbox();
		m_followBox.setChecked(true);
		btn.add(" ");
		btn.add(m_followBox);
		btn.add("Follow ");
		m_followBox.setOnValueChanged(new IValueChanged<Checkbox>() {
			@Override
			public void onValueChanged(@Nonnull Checkbox component) throws Exception {
				if(component.isChecked())
					updateLinesPerPage();
			}
		});

		List<ValueLabelPair<Integer>> szl = new ArrayList<ValueLabelPair<Integer>>();
		for(int size : SIZES) {
			szl.add(new ValueLabelPair<Integer>(Integer.valueOf(size), Integer.toString(size)));
		}
		m_linesCombo = new ComboFixed<Integer>(szl);
		m_linesCombo.setValue(Integer.valueOf(32));
		m_linesCombo.setMandatory(true);
		m_linesCombo.setOnValueChanged(new IValueChanged<ComboFixed<Integer>>() {
			@Override
			public void onValueChanged(@Nonnull ComboFixed<Integer> component) throws Exception {
				updateLinesPerPage();
			}
		});
		btn.add(m_linesCombo);
		btn.add(" lines/page");

		//-- Size lines
		Div sz = new Div();
		ttl.add(sz);
		sz.setCssClass("ui-tlf-sz");
		m_lineSpan = new Span();
		sz.add(m_lineSpan);
		sz.add(" lines, ");
		m_szSpan = new Span();
		sz.add(m_szSpan);

		//-- And last - the log name
		ttl.add(m_logpath);					// The ref to what we see.
	}


	private void gotoLine(int linenr) throws Exception {
		int maxline = m_task.getLastLine() - getLinesPerPage();
		if(maxline < 0)
			maxline = 0;
		if(linenr > maxline)
			linenr = maxline;
		if(m_startLine == linenr)
			return;

		//-- If this is before the last line then disable "follow"
		if(linenr < maxline)
			m_followBox.setChecked(false);						// No longer follow.
		m_goto.setValue(null);
		updateLines(linenr);
	}

	/**
	 * Only update the "log file size" parts in the title.
	 */
	private void updateSizes() {
		long sz = m_task.getSize();
		int ln = m_task.getLastLine();

		m_lineSpan.setText(StringTool.strCommad(ln));
		m_szSpan.setText(StringTool.strSize(sz));
		m_oldLines = ln;
		m_oldSize = sz;
	}

	@Override
	public void checkForChanges() throws Exception {
		if (m_noFile){
			return;
		}
		m_task.readFileDelta();
		long sz = m_task.getSize();
		int ln = m_task.getLastLine();

		if(m_oldLines != ln || m_oldSize != sz)
			updateSizes();

		//-- Is it time to update the lines?
		int xsline = isTailing() ? ln - getLinesPerPage() : m_startLine;				// The start line we would expect from current state
		if(xsline < 0)
			xsline = 0;
		int xeline = xsline + getLinesPerPage();										// The end line we would expect from current status.
		if(xeline > ln)
			xeline = ln;
		int veline = m_startLine + m_lineDiv.getChildCount();				// The actual line# of the end line
		if(xsline != m_startLine || xeline != veline)						// If either start or end changed in between: update
			updateLines(xsline);
	}

	private void updateLinesPerPage() throws Exception {
		int lpp = getLinesPerPage();
		int cln = m_task.getLastLine() - lpp;

		//-- If this would exceed the end move current line back
		int newln = m_startLine;
		if(isTailing()) {
			newln = cln;
		} else if(newln > cln)
			newln = cln;
		m_lineDiv.removeAllChildren();
		updateLines(newln);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Update line fragments.								*/
	/*--------------------------------------------------------------*/

	private boolean isTailing() {
		return m_followBox.isChecked();
	}

	/**
	 * Update the visible lines according to startLine.
	 * @throws IOException
	 */
	private void updateLines(int newstart) throws Exception {
		if(newstart < 0)
			newstart = 0;
		int vlines = m_lineDiv.getChildCount();
		int linesPerPage = getLinesPerPage();
		int xeline = newstart + linesPerPage;									// Expected end line in new visible part
		int maxlines = m_task.getLastLine();
		if(xeline > maxlines)
			xeline = maxlines;

		//		m_debugDiv.setText(">>> want start at line " + newstart + ", end=" + (newstart + getLinesPerPage()) + ", lineset=" + newstart + ":" + xeline);

		//-- Get the lineset to show.
		List<String> lines = m_task.getLines(newstart, xeline);

		//-- Where must we insert the new lines?
		int insix = newstart - m_startLine;			// Where is the new location relative to the currently visible fragment?
		if(insix <= -vlines || insix > vlines) {
			//-- Nothing in the visible line list can be retained: create a complete new set of lines.
			m_lineDiv.removeAllChildren();
			m_startLine = newstart;

			for(int i = 0; i < linesPerPage; i++) {
				if(i + newstart >= xeline)
					break;
				if(i >= lines.size())
					throw new IllegalStateException("Attempting to get index " + i + " from lines with size " + lines.size());
				NodeContainer l = createLine(newstart + i, lines.get(i));
				m_lineDiv.add(l);
			}
			return;
		}

		if(insix > 0) {
			//-- We need to remove insix lines from the *start* of the visible presentation. After that it *starts" at newstart, and we just need to fill the remainder
			while(insix > 0 && m_lineDiv.getChildCount() > 0) {
				m_lineDiv.getChild(0).remove();
			}
			m_startLine = newstart;
		} else if(insix < 0) {
			//-- We need to insert insix lines *before* the current presentation while removing everything that then overflows the div...
			int i = 0;
			while(insix < 0) {
				String txt = lines.get(i);
				NodeContainer l = createLine(newstart + i, txt);
				m_lineDiv.add(i, l);
				i++;
				insix++;
			}
			m_startLine = newstart;

			while(m_lineDiv.getChildCount() > linesPerPage)
				// Drop all lines that now extend too far
				m_lineDiv.getChild(m_lineDiv.getChildCount() - 1).remove();
		}

		/*
		 * We now have a visible presentation that *always* starts at the new position, but it can be too short- so add everything we can.
		 */
		int ix = m_lineDiv.getChildCount();			// Current "last location" in visible presentation
		while(ix < linesPerPage && ix < lines.size()) {						// Can we have more?
			NodeContainer l = createLine(newstart + ix, lines.get(ix));
			m_lineDiv.add(l);
			ix++;
		}
	}

	private NodeContainer createLine(int lnr, String txt) {
		Div line = new Div();
		line.setCssClass("ui-tlf-line");
		Span ld = new Span();
		line.add(ld);
		ld.setCssClass("ui-tlf-lnr");
		ld.setText(StringTool.strCommad(lnr));
		Span l = new Span();
		line.add(l);
		l.setCssClass("ui-tlf-l");
		l.setText(txt);
		return line;
	}

	private int getLinesPerPage() {
		Integer value = m_linesCombo.getValue();
		if(null == value)
			return 50;
		return value.intValue();
	}
}
