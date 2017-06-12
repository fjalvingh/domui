package to.etc.domui.component.wizard;

import to.etc.domui.dom.html.*;

/**
 * Required interface for creating {@link WizardPopupWindow} steps.
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 6-6-17.
 */
public interface IWizardPopupStep {

	public static final String IS_DISABLED = "disabled";

	Div getWizardPopupStepDiv() throws Exception;

	void createContent() throws Exception;

	boolean m_isDisabled = false;

	boolean isDisabled();
}
