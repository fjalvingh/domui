# DomUI Wizard Popup Component

A generic wizard component that can be opened in a DomUI Window. Some parameters that developers can set:

  - Set custom button labels for each wizard step;
  - Set the position of the navigation bar; choose between horizontal or vertical alignment;
  - For every step, it is possible to set whether it is valid to go to the next step;
  - Exchange variables for every step easily by using a step's key value storage.

# Instantiate a custom wizard

First, create a class that extends AbstractWizardPopupGeneratorBase, that is responsible for creating and customizing the wizard. For instance, a class that holds a wizard to send something in bulk.

Next, add a custom constructor, like the following. Of course, you can add more variables if needed.
  
  ```
	public BulkBriefWizard() {
        setupWizard();
	}
  ```

The setupWizard method is responsible for creating and customizing the wizard. You should implement it. The method can look like this:

```
    @Override
    protected void setupWizard() {
        addStep("First step"), new BulkBriefSelectCaseType(m_model.getAllCaseTypeList())).set();
        addStep("Second step"), new BulkBriefSelectCases(m_model)).nextButton("Are you really sure?").set();
        addStep("Third step", new BulkBriefTest()).backButton("Get me outta here!").nextButton("Okay").set();
        addStep("Final step", new BulkBriefTest()).backButton("I changed my mind").endButton("Send!").set();
        setWizardTitle("Send something");
        setValidIcon(Theme.BTN_CONFIRM);
        setBackIcon(Theme.BTN_MOVE_LEFT);
        setCancelIcon(Theme.BTN_CANCEL);
        setEndIcon(Theme.BTN_CONFIRM);
        setNextIcon(Theme.BTN_MOVE_RIGHT);
        initWizard(true);
    }
```

As is shown, various variables can be set up. First, add the name of the step. Next, you can add some custom labels for each step buttons in a building pattern way. Currently, nextButton, endButton, cancelButton, and backButton are supported. Then, you can select button icons that the wizard could display. This is optional. Finally, add a class that extends AbstractWizardStepBase. This class is the step's body.

> **Important:** At the end of the setup, you should initialize the wizard with the initWizard method. When set to true, the wizard will be displayed horizontally. Otherwise, a vertical wizard will be initialized.

# Using the step storage

For every step, it is possible to store some information that can be used in other steps. To achieve this, you have to setup a key value first and require it later. 

First, add the value you want to retrieve later to the step's storage. You could do that like this:

```
	String valueFromModel = model.getValue();
        if(null != currentValue) {
            WizardPopupStepStorageType<String> valueToSave = new WizardPopupStepStorageType<>();
            valueToSave.set(valueFromModel);
            addToStorage(ExtendsPopupGeneratorBaseClass.KEY_CONSTANT, valueToSave);
	}
```

Next, in the next fase, you could retrieve the value like this:

```
	AbstractWizardPopupStepBase previousStep = getPreviousStep();
	WizardPopupStepStorageType<?> storedValue = previousStep.getStorageItem(ExtendsPopupGeneratorBaseClass.KEY_CONSTANT);
	String value = (String) storedValue.get();
```

# Disable next button when step is invalid

This wizard component can check whether a certain step is valid or not before going to the next step. In order to do that, simply extend the isDisabled() method from the AbstractWizardPopupStepBase.
