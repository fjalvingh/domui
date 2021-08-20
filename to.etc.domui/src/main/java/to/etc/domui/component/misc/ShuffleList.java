package to.etc.domui.component.misc;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Select;
import to.etc.domui.dom.html.SelectOption;
import to.etc.util.StringTool;
import to.etc.webapp.ProgrammerErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component containing two lists of items - left list containing available items and right list containing selected items.
 * There are also buttons to move items between lists and to sort items in the right list.
 *
 * Precondition is that each item has a unique label. Otherwise we'll be getting an exception.
 * Uniqueness of the label should be handled outside this component.
 *
 * @param <T> Type of the items in the lists. They are represented by the (unique) label, but underneath we can have any entity.
 */
public class ShuffleList<T> extends Div {

	private final List<ValueLabelPair<T>> availableItems;
	private final List<ValueLabelPair<T>> selectedItems;

	private Select left;
	private Select right;

	public ShuffleList(List<ValueLabelPair<T>> availableItems, List<ValueLabelPair<T>> selectedItems) {
		this.availableItems = availableItems;
		this.selectedItems = selectedItems;

		validateLists();
	}

	private void validateLists() {
		List<String> labels = new ArrayList<>();
		for (ValueLabelPair<T> prop : availableItems) {
			if (labels.contains(prop.getLabel())) {
				throw new ProgrammerErrorException("You cannot have multiple entries with same label in a shuffle list. Duplicate found: " + prop.getLabel());
			}
			labels.add(prop.getLabel());
		}
		for (ValueLabelPair<T> prop : selectedItems) {
			if (labels.contains(prop.getLabel())) {
				throw new ProgrammerErrorException("You cannot have multiple entries with same label in a shuffle list. Duplicate found: " + prop.getLabel());
			}
			labels.add(prop.getLabel());
		}
	}

	public List<T> getAvailableItems() {
		return availableItems.stream()
			.map(ValueLabelPair::getValue)
			.collect(Collectors.toList());
	}

	public List<T> getSelectedItems() {
		return selectedItems.stream()
			.map(ValueLabelPair::getValue)
			.collect(Collectors.toList());
	}

	@Override
	public void createContent() throws Exception {
		left = createSelect(availableItems);
		Div leftRightButtonsDiv = createLeftRightButtonsDiv();
		right = createSelect(selectedItems);
		Div upDownButtonsDiv = createUpDownButtonsDiv();

		FormBuilder fb = new FormBuilder(this);
		fb.horizontal();

		fb.control(left);
		fb.control(leftRightButtonsDiv);
		fb.control(right);
		fb.control(upDownButtonsDiv);
	}

	private Select createSelect(List<ValueLabelPair<T>> items) {
		String[] labels = items.stream().map(ValueLabelPair::getLabel).toArray(String[]::new);

		Select s = new Select(labels);
		s.setHeight("300px");
		s.setWidth("150px");
		s.setMultiple(true);
		return s;
	}

	private Div createLeftRightButtonsDiv() {
		Div div = new Div();
		div.setDisplay(DisplayType.TABLE_CAPTION);
		div.setTextAlign(TextAlign.CENTER);

		DefaultButton addBtn = new DefaultButton(">", clickednode -> addItems());
		addBtn.setWidth("35px");
		div.add(addBtn);
		DefaultButton addAllBtn = new DefaultButton(">>", clickednode -> addAll());
		addAllBtn.setWidth("35px");
		div.add(addAllBtn);
		DefaultButton removeBtn = new DefaultButton("<", clickednode -> removeItems());
		removeBtn.setWidth("35px");
		div.add(removeBtn);
		DefaultButton removeAllBtn = new DefaultButton("<<", clickednode -> removeAll());
		removeAllBtn.setWidth("35px");
		div.add(removeAllBtn);

		return div;
	}

	private void addItems() {
		String itemLabel = moveItem(left, right);
		moveItemInModel(itemLabel, availableItems, selectedItems);
	}

	private void removeItems() {
		String itemLabel = moveItem(right, left);
		moveItemInModel(itemLabel, selectedItems, availableItems);
	}

	private String moveItem(Select from, Select to) {
		int selectedIndex = from.getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}

		String selected = from.getOption(selectedIndex).getTextContents();

		from.removeChild(selectedIndex);
		from.setSelectedIndex(-1);

		to.add(new SelectOption(selected));

		return selected;
	}

	private void moveItemInModel(String itemLabel, List<ValueLabelPair<T>> from, List<ValueLabelPair<T>> to) {
		if (StringTool.isBlank(itemLabel)) {
			return;
		}

		ValueLabelPair<T> item = getItemByLabel(itemLabel, from);

		from.remove(item);
		to.add(item);
	}

	private ValueLabelPair<T> getItemByLabel(String itemLabel, List<ValueLabelPair<T>> list) {
		return list.stream()
			.filter(i -> itemLabel.equals(i.getLabel()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Item with label " + itemLabel + " not found."));
	}

	private void addAll() {
		moveAllItems(left, right);
		moveAllItemsInModel(availableItems, selectedItems);
	}

	private void removeAll() {
		moveAllItems(right, left);
		moveAllItemsInModel(selectedItems, availableItems);
	}

	private void moveAllItems(Select from, Select to) {
		from.getChildren(SelectOption.class).forEach(to::add);
		from.removeAllChildren();
		from.setSelectedIndex(-1);
	}

	private void moveAllItemsInModel(List<ValueLabelPair<T>> from, List<ValueLabelPair<T>> to) {
		to.addAll(from);
		from.clear();
	}

	private Div createUpDownButtonsDiv() {
		Div div = new Div();
		div.setDisplay(DisplayType.TABLE_CAPTION);

		div.add(new DefaultButton("/\\", clickednode -> moveItemUp()));
		div.add(new DefaultButton("\\/", clickednode -> moveItemDown()));

		return div;
	}

	private void moveItemUp() {
		int selectedIndex = right.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		if (selectedIndex == 0) {
			// cannot move the highest element up
			return;
		}
		int indexToSwapWith = selectedIndex - 1;
		swapItems(selectedIndex, indexToSwapWith);
	}

	private void moveItemDown() {
		int selectedIndex = right.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		if (selectedIndex == right.getChildCount() - 1) {
			// cannot move the lowest element down
			return;
		}
		int indexToSwapWith = selectedIndex + 1;
		swapItems(selectedIndex, indexToSwapWith);
	}

	private void swapItems(int i1, int i2) {
		String label1 = right.getOption(i1).getTextContents();
		String label2 = right.getOption(i2).getTextContents();

		right.getOption(i1).setText(label2);
		right.getOption(i2).setText(label1);

		right.setSelectedIndex(i2);

		Collections.swap(selectedItems, i1, i2);
	}

}
