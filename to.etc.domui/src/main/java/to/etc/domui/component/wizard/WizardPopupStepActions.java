package to.etc.domui.component.wizard;

import to.etc.domui.component.menu.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * Holds all actions for {@link WizardPopupStep}
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
public enum WizardPopupStepActions {
	CANCEL,
	PREVIOUS,
	NEXT,
	FINISH;

	public IUIAction<Void> getAction(@Nonnull WizardPopupWindow wizard) {
		switch(this) {
			case CANCEL:
				return cancelStep(wizard);
			case PREVIOUS:
				return toPreviousStep(wizard);
			case NEXT:
				return toNextStep(wizard);
			case FINISH:
				return cancelStep(wizard);
			default:
				throw new IllegalStateException("This wizard action is not implemented!");
		}
	}

	private IUIAction<Void> cancelStep(@Nonnull WizardPopupWindow wizard) {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return WizardPopupStepActions.CANCEL.toString();
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				wizard.closePressed();
			}
		};
	}

	private IUIAction<Void> toNextStep(@Nonnull WizardPopupWindow wizard) {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return WizardPopupStepActions.NEXT.toString();
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				ArrayList<WizardPopupStep> steps = wizard.getSteps();
				WizardPopupStep currentStep = wizard.getCurrentStep();
				if(!currentStep.getStepBody().isDisabled()) {
					int nextStep = steps.indexOf(currentStep) + 1;
					if(nextStep > 0 && nextStep < steps.size()) {
						wizard.setCurrentStep(steps.get(nextStep));
						wizard.refreshNavbarContainer();
						wizard.refreshBodyContainer();
					}
				}
			}
		};
	}

	private IUIAction<Void> toPreviousStep(@Nonnull WizardPopupWindow wizard) {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return WizardPopupStepActions.PREVIOUS.toString();
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				ArrayList<WizardPopupStep> steps = wizard.getSteps();
				WizardPopupStep currentStep = wizard.getCurrentStep();
				int previousStep = steps.indexOf(currentStep) - 1;
				if(previousStep >= 0 && previousStep < steps.size()) {
					wizard.setCurrentStep(steps.get(previousStep));
					wizard.refreshNavbarContainer();
					wizard.refreshBodyContainer();
				}
			}
		};
	}
}
