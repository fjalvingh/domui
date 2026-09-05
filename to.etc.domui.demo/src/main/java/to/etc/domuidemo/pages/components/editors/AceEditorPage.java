package to.etc.domuidemo.pages.components.editors;

import to.etc.domui.component.ace.AceEditor;
import to.etc.domui.component.ace.AceEditor.AceWrapMode;
import to.etc.domui.component.ace.AceEditor.Completion;
import to.etc.domui.component.ace.PositionCalculator;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AceEditor: the embedded code editor, with completion, markers and the rest.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class AceEditorPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("AceEditor");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "AceEditor"));

		AceEditor editor = new AceEditor();
		editor.setMode("ace/mode/javascript");            // The full path, for the language
		editor.setTheme("iplastic");                      // ...but the bare name, for the theme
		editor.setWidth("100%");
		editor.setHeight("300px");                        // Without a size it does not show
		cp.add(editor);

		editor.setValue(FileTool.readResourceAsString(getClass(), "demojs.js", "utf-8"));
		editor.setCompletionHandler(this::completeCode);

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Other text", a -> editor.setValue(FileTool.readResourceAsString(getClass(), "demo2js.js", "utf-8")));
		bb.addButton("Clear", a -> editor.setValue(null));
		bb.addButton("Read only", a -> editor.setReadOnly(!editor.isReadOnly()));
		bb.addButton("Show the value", a -> MsgBox2.on(this).info(String.valueOf(editor.getValue())));
		bb.addButton("Show the selection", a -> MsgBox2.on(this).info(String.valueOf(editor.getSelectedText())));
		bb.addButton("Mark every 'var'", a -> markVars(editor));
		bb.addButton("Clear the markers", a -> editor.markerClear());
		bb.addButton("Insert at the cursor", a -> editor.insertAtCursor("/* inserted from the server */\n"));
		bb.addButton("Go to line 5", a -> editor.gotoLine(5));
		bb.addButton("Wrap", a -> editor.setWrapMode(AceWrapMode.Wrap));
		bb.addButton("Do not wrap", a -> editor.setWrapMode(AceWrapMode.None));

		cp.add(new Para().add("The editor is an IControl<String> whose value is the text being "
			+ "edited, and everything else is done by calling it: move the caret, select a range, "
			+ "insert at the cursor, switch wrapping on. getSelectedText() gives what the user has "
			+ "selected, which arrives with every value change."));

		cp.add(new HTag(2, "Completion"));
		cp.add(new Para().add("Press CTRL+SPACE somewhere in the text. The editor asks the "
			+ "completion handler for a list of possibilities, given the whole text, the row and "
			+ "column of the caret, and the word typed so far; the one below offers every word in "
			+ "the document that contains that prefix."));

		cp.add(new HTag(2, "Markers"));
		cp.add(new Para().add("Press \"Mark every 'var'\": each one gets a warning marker with a "
			+ "message on it, which is how a compiler's errors are shown in the editor. "
			+ "PositionCalculator turns a character offset in the text into the line and column a "
			+ "marker needs."));
	}

	/**
	 * Put a warning marker on every occurrence of the word "var" in the text.
	 */
	private void markVars(AceEditor editor) {
		String value = editor.getValue();
		if(null == value) {
			return;
		}
		editor.markerClear();

		PositionCalculator pc = new PositionCalculator(value);
		Point start = new Point();
		Point end = new Point();
		int ix = 0;
		while(ix < value.length()) {
			int pos = value.indexOf("var", ix);
			if(pos == -1) {
				break;
			}
			pc.getXYPosition(start, pos);                 // The offset, as a line and a column
			pc.getXYPosition(end, pos + 3);
			editor.markerAdd(MsgType.WARNING, start.y, start.x, end.y, end.x, "Use 'let' instead of 'var', it is safer");
			ix = pos + 3;
		}
	}

	/**
	 * The completion handler: every word in the document that contains what was typed.
	 */
	private List<Completion> completeCode(String text, int row, int col, String prefix) {
		Set<String> words = new HashSet<>(Arrays.asList(text.split("\\W+")));
		String prefixLC = prefix.toLowerCase();
		return words.stream()
			.filter(a -> a.toLowerCase().contains(prefixLC))
			.map(a -> new Completion(a, a, "Word", 10))
			.collect(Collectors.toList());
	}
}
