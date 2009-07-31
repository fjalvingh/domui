package to.etc.domui.component.lookup;

public class CustomSearchField {
	private String labelCaption;

	private ILookupControlInstance queryBuilderThingy;

	public CustomSearchField(String labelCaption, ILookupControlInstance queryBuilderThingy) {
		super();
		this.labelCaption = labelCaption;
		this.queryBuilderThingy = queryBuilderThingy;
	}

	public String getLabelCaption() {
		return labelCaption;
	}

	public ILookupControlInstance getQueryBuilderThingy() {
		return queryBuilderThingy;
	}
}
