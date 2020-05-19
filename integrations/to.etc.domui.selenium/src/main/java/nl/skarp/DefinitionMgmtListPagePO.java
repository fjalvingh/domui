package nl.skarp;

import to.etc.domui.webdriver.core.base.InputPO;
import to.etc.domui.webdriver.core.base.LookupPO;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.core.base.BasePO;
import to.etc.domui.webdriver.core.base.ButtonPO;


@NonNullByDefault()
public class DefinitionMgmtListPagePO extends BasePO {

	private InputPO m_code = new InputPO(wd(), "Code");
	private InputPO m_gebruikt_door_any = new InputPO(wd(), "Gebruikt door (*=any)");
	private InputPO m_formule_bevat = new InputPO(wd(), "Formule bevat");
	private InputPO m_name = new InputPO(wd(), "name");
	private LookupPO m_eSectormodel = new LookupPO(wd(), "ESectormodel", FIXME);
	private LookupPO m_owner = new LookupPO(wd(), "owner", FIXME);
	private ButtonPO m_searchButton = new ButtonPO(wd(), "searchButton");
	private ButtonPO m_clearButton = new ButtonPO(wd(), "clearButton");
	private ButtonPO m_hideButton = new ButtonPO(wd(), "hideButton");
	private ButtonPO m_createPI = new ButtonPO(wd(), "createPI");
	private ButtonPO m_createME = new ButtonPO(wd(), "createME");
	private ButtonPO m_createBR = new ButtonPO(wd(), "createBR");
	private ButtonPO m_createCR = new ButtonPO(wd(), "createCR");
	private ButtonPO m_createDI = new ButtonPO(wd(), "createDI");
	private ButtonPO m_createCO = new ButtonPO(wd(), "createCO");
	private ButtonPO m_createOO = new ButtonPO(wd(), "createOO");

	public DefinitionMgmtListPagePO(WebDriverConnector wd) {
		super(wd);
	}
	public InputPO getCode() {
		return m_code;
	}
	public InputPO getGebruikt_door_any() {
		return m_gebruikt_door_any;
	}
	public InputPO getFormule_bevat() {
		return m_formule_bevat;
	}
	public InputPO getName() {
		return m_name;
	}
	public LookupPO getESectormodel() {
		return m_eSectormodel;
	}
	public LookupPO getOwner() {
		return m_owner;
	}
	public ButtonPO getSearchButton() {
		return m_searchButton;
	}
	public ButtonPO getClearButton() {
		return m_clearButton;
	}
	public ButtonPO getHideButton() {
		return m_hideButton;
	}
	public ButtonPO getCreatePI() {
		return m_createPI;
	}
	public ButtonPO getCreateME() {
		return m_createME;
	}
	public ButtonPO getCreateBR() {
		return m_createBR;
	}
	public ButtonPO getCreateCR() {
		return m_createCR;
	}
	public ButtonPO getCreateDI() {
		return m_createDI;
	}
	public ButtonPO getCreateCO() {
		return m_createCO;
	}
	public ButtonPO getCreateOO() {
		return m_createOO;
	}
}
