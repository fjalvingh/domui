package to.etc.domui.component.wizard;

import javax.annotation.*;
import java.util.*;

/**
 * This class is the abstract base for a WizardPopupWindow generator (i.e. the class that instantiates the {@link WizardPopupWindow}).
 * A generator must extend this base in order to implement all necessary methods.
 *
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 12-6-17.
 */
public abstract class WizardPopupGeneratorBase {

	abstract public WizardPopupWindow getWizard();

	abstract public Map<String, Object> getStepStorage();

	abstract public void addToStepStorage(@Nonnull Object o);

	abstract public void removeFromStepStorage();

	private Map<Map<String, String>, IWizardPopupStep> setupSteps(){
		return null;
	}
}
