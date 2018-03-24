package to.etc.domui.log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.controlfactory.ModelBindings;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.component.misc.UIControlUtil;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.css.FontStyle;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IUserInputModifiedFence;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.log.data.Filter;
import to.etc.domui.log.data.Handler;
import to.etc.domui.log.data.HandlerType;
import to.etc.domui.log.data.LoggerRootDef;
import to.etc.domui.log.data.Matcher;
import to.etc.domui.state.UIGoto;
import to.etc.domui.util.Msgs;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.Level;
import to.etc.log.handler.EtcLogFormat;
import to.etc.log.handler.LogFilterType;
import to.etc.webapp.nls.BundleRef;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class LoggerConfigPage extends UrlPage implements IUserInputModifiedFence {
	static final BundleRef BUNDLE = Msgs.BUNDLE;

	private ButtonBar m_buttonBar;

	private DefaultButton m_saveButton;

	private DefaultButton m_cancelButton;

	private boolean m_modified;

	private Label m_notSavedInfo;

	private final List<Handler> m_handlers = new ArrayList<Handler>();

	private ConfigPart m_configPart;

	private LoggerRootDef m_rootDef;

	private ModelBindings m_rootDefBindings;

	@Override
	public void createContent() throws Exception {
		super.createContent();
		createButtonBar();
		createButtons();

	}

	protected void createButtonBar() {
		add(getButtonBar());
	}

	public ButtonBar getButtonBar() {
		if(m_buttonBar == null) {
			m_buttonBar = new ButtonBar();
		}
		return m_buttonBar;
	}

	protected void createButtons() throws Exception {
		createCommitButton();
		createCancelButton();
		createConfigPanel();
	}

	private void createConfigPanel() throws Exception {
		addRootConfigPart();
		org.w3c.dom.Document doc = EtcLoggerFactory.getSingleton().toXml(true);
		loadXml(doc);
		m_configPart = new ConfigPart(m_handlers);
		add(m_configPart);
	}

	private void loadXml(Document doc) {
		m_handlers.clear();
		NodeList handlerNodes = doc.getElementsByTagName("handler");
		for(int i = 0; i < handlerNodes.getLength(); i++) {
			Node handlerNode = handlerNodes.item(i);
			m_handlers.add(loadHandler(handlerNode));
		}
	}

	private Handler loadHandler(Node handlerNode) {
		HandlerType type = "file".equalsIgnoreCase(handlerNode.getAttributes().getNamedItem("type").getNodeValue()) ? HandlerType.FILE : HandlerType.STDOUT;
		String file = null;
		if(type == HandlerType.FILE) {
			file = handlerNode.getAttributes().getNamedItem("file").getNodeValue();
		}
		Handler handler = new Handler(type, file);
		NodeList nodes = handlerNode.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if("log".equals(node.getNodeName())) {
				handler.addMatcher(loadMatcher(node));
			} else if("filter".equals(node.getNodeName())) {
				handler.addFilter(loadFilter(node));
			} else if("format".equals(node.getNodeName())) {
				handler.setFormat(loadFormat(node));
			}
		}
		if(handler.getFormat() == null) {
			handler.setFormat(EtcLogFormat.DEFAULT);
		}
		return handler;
	}

	private String loadFormat(Node node) {
		String format = node.getAttributes().getNamedItem("pattern").getNodeValue();
		return format;
	}

	private Matcher loadMatcher(Node node) {
		String name = node.getAttributes().getNamedItem("name").getNodeValue();
		Level level = Level.valueOf(node.getAttributes().getNamedItem("level").getNodeValue());
		return new Matcher(name, level);
	}

	private Filter loadFilter(Node node) {
		LogFilterType type = "mdc".equalsIgnoreCase(node.getAttributes().getNamedItem("type").getNodeValue()) ? LogFilterType.MDC : LogFilterType.SESSION;
		String key = "session";
		if(type != LogFilterType.SESSION) {
			key = node.getAttributes().getNamedItem("key").getNodeValue();
		}
		String value = node.getAttributes().getNamedItem("value").getNodeValue();
		return new Filter(type, key, value);
	}

	private void addRootConfigPart() throws Exception {
		m_rootDef = new LoggerRootDef(EtcLoggerFactory.getSingleton().getLogDir(), EtcLoggerFactory.getSingleton().logDirOriginalAsConfigured(), EtcLoggerFactory.getSingleton().getLogDir());
		FormBuilder fb = new FormBuilder(this);

		fb.property(m_rootDef, LoggerRootDef.pROOTDIR).control();
		fb.property(m_rootDef, LoggerRootDef.pLOGDIR).control();

		if(!m_rootDef.getLogDir().equals(m_rootDef.getLogDirAbsolute())) {
			Label calculatedPath = new Label(m_rootDef.getLogDirAbsolute());
			fb.label("log dir path").item(calculatedPath);
		}
	}

	protected void createCommitButton() {
		m_saveButton = getButtonBar().addButton(BUNDLE.getString(Msgs.EDLG_OKAY), Msgs.BTN_SAVE, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				save();
			}
		});
		//hide by default, it would become visible if modifications on page are detected
		m_saveButton.setDisabled(true);
		m_saveButton.setTitle("no changes to save");
	}

	protected void createCancelButton() {
		m_cancelButton = getButtonBar().addButton(BUNDLE.getString(Msgs.EDLG_CANCEL), Msgs.BTN_CANCEL, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				reloadPageData();
			}
		});
		//hide by default, it would become visible if modifications on page are detected
		m_cancelButton.setDisabled(true);
	}

	protected void onAfterSave() throws Exception {
		reloadPageData();
		MessageFlare.display(this, MsgType.INFO, $("data.saved"));
	}

	protected void save() throws Exception {
		if(validateData()) {
			onSave();
			onAfterSave();
		}
	}

	private boolean validateData() throws Exception {
		m_rootDefBindings.moveControlToModel();
		return m_configPart.validateData();
	}

	private void onSave() throws Exception {
		org.w3c.dom.Document doc = toXml();
		EtcLoggerFactory.getSingleton().loadConfig(doc);
		EtcLoggerFactory.getSingleton().saveConfig();
	}

	private Document toXml() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element rootElement = doc.createElement("config");
		doc.appendChild(rootElement);
		rootElement.setAttribute("logLocation", m_rootDef.getLogDir());

		for(Handler handler : m_handlers) {
			Element handlerNode = doc.createElement("handler");
			rootElement.appendChild(handlerNode);
			handler.saveToXml(doc, handlerNode);
		}
		return doc;
	}

	protected void reloadPageData() throws Exception {
		UIGoto.reload();
	}

	@Override
	public boolean isModified() {
		return m_modified;
	}

	@Override
	public void setModified(boolean modified) {
		m_modified = modified;
	}

	@Override
	public boolean isFinalUserInputModifiedFence() {
		return true;
	}

	@Override
	public void onModifyFlagRaised() {
		if(m_saveButton != null) {
			m_saveButton.setDisabled(false);
			m_saveButton.setTitle(null);
		}
		if(m_cancelButton != null) {
			m_cancelButton.setDisabled(false);
		}
		if(m_notSavedInfo == null) {
			addNotSavedWarning();
		}
	}

	private void addNotSavedWarning() {
		if(m_buttonBar != null && m_notSavedInfo == null) {
			m_notSavedInfo = new Label($("data.modified"));
			m_notSavedInfo.setFontStyle(FontStyle.ITALIC);
			m_notSavedInfo.setColor(UIControlUtil.getRgbHex(java.awt.Color.RED, true));
			m_buttonBar.appendAfterMe(m_notSavedInfo);
		}
	}
}
