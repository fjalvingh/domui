package to.etc.domui.component.wizard;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * Holds the fragment for the body of a {@link WizardPopupWindow} step.
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
@DefaultNonNull
class WizardPopupStepBodyFragment extends Div {

	private static final BundleRef BUNDLE = BundleRef.create(WizardPopupWindow.class, "messages");

	private String m_layout;

	private String m_previousButtonLabel;

	private String m_nextButtonLabel;

	private String m_cancelButtonLabel;

	private String m_endButtonLabel;

	private Map<String, IUIAction<Void>> m_buttonActions;

	private boolean m_isFirstStep;

	private boolean m_isLastStep;

	private AbstractWizardPopupStepBase m_step;

	@Nullable
	private String m_cancelIcon;

	@Nullable
	private String m_nextIcon;

	@Nullable
	private String m_previousIcon;

	@Nullable
	private String m_endIcon;

	WizardPopupStepBodyFragment(@Nonnull AbstractWizardPopupStepBase step, @Nonnull Map<String, String> labels, @Nonnull Map<String, IUIAction<Void>> buttonActions, boolean isFirstStep, boolean isLastStep, @Nonnull String layout, @Nullable Map<String, String> icons) {
		m_previousButtonLabel = labels.get(BUNDLE.getString("label.indicator.back"));
		m_cancelButtonLabel = labels.get(BUNDLE.getString("label.indicator.cancel"));
		m_nextButtonLabel = labels.get(BUNDLE.getString("label.indicator.next"));
		m_endButtonLabel = labels.get(BUNDLE.getString("label.indicator.end"));
		m_step = step;
		m_buttonActions = buttonActions;
		m_isFirstStep = isFirstStep;
		m_isLastStep = isLastStep;
		m_layout = layout;

		if(null != icons) {
			m_previousIcon = icons.get(BUNDLE.getString("icons.indicator.back"));
			m_cancelIcon = icons.get(BUNDLE.getString("icons.indicator.cancel"));
			m_nextIcon = icons.get(BUNDLE.getString("icons.indicator.next"));
			m_endIcon = icons.get(BUNDLE.getString("icons.indicator.end"));
		}
	}

	@Override
	public void createContent() throws Exception {
		Div stepBodyContainer = new Div();
		stepBodyContainer.setCssClass("ui-gwiz-body-container" + m_layout);

		Div stepBody = new Div();
		stepBody.add(m_step);
		stepBody.setCssClass("ui-gwiz-body" + m_layout);

		stepBodyContainer.add(stepBody);
		add(stepBodyContainer);

		Div buttonContainer = new Div();
		buttonContainer.setCssClass("ui-gwiz-body-footer" + m_layout);

		DefaultButton cancelButton = new DefaultButton(m_buttonActions.get(BUNDLE.getString("action.indicator.cancel")));
		cancelButton.setText(m_cancelButtonLabel);
		String cancelIcon = m_cancelIcon;
		if(null != cancelIcon) {
			cancelButton.setIcon(cancelIcon);
		}

		ButtonBar leftButtonBar = new ButtonBar();
		leftButtonBar.setPosition(PositionType.ABSOLUTE);
		leftButtonBar.setTop("8px");
		leftButtonBar.setLeft("5px");
		leftButtonBar.setFloat(FloatType.LEFT);
		leftButtonBar.setWidth("50%");
		leftButtonBar.setTextAlign(TextAlign.LEFT);

		leftButtonBar.addButton(cancelButton);
		buttonContainer.add(leftButtonBar);

		DefaultButton previousButton = new DefaultButton(m_buttonActions.get(BUNDLE.getString("action.indicator.back")));
		previousButton.setText(m_previousButtonLabel);
		String previousIcon = m_previousIcon;
		if(null != previousIcon) {
			previousButton.setIcon(previousIcon);
		}

		if(m_isFirstStep) {
			previousButton.setDisabled(true);
		}

		DefaultButton nextEndButton;
		if(m_isLastStep) {
			nextEndButton = new DefaultButton(m_buttonActions.get(BUNDLE.getString("action.indicator.cancel")));
			nextEndButton.setText(m_endButtonLabel);
			String endIcon = m_endIcon;
			if(null != endIcon) {
				nextEndButton.setIcon(endIcon);
			}
		} else {
			nextEndButton = new DefaultButton(m_buttonActions.get(BUNDLE.getString("action.indicator.next")));
			nextEndButton.setText(m_nextButtonLabel);
			String nextIcon = m_nextIcon;
			if(null != nextIcon) {
				nextEndButton.setIcon(nextIcon);
			}
		}
		nextEndButton.bind("disabled").to(m_step, AbstractWizardPopupStepBase.IS_VALID);

		ButtonBar rightButtonBar = new ButtonBar();
		rightButtonBar.setPosition(PositionType.ABSOLUTE);
		rightButtonBar.setTop("8px");
		rightButtonBar.setRight("0px");
		rightButtonBar.setFloat(FloatType.RIGHT);
		rightButtonBar.setWidth("50%");
		rightButtonBar.setTextAlign(TextAlign.RIGHT);

		rightButtonBar.addButton(previousButton);
		rightButtonBar.addButton(nextEndButton);

		buttonContainer.add(rightButtonBar);
		add(buttonContainer);
	}
}
