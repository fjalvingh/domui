package to.etc.domui.component.lookup;

public class CustomSearchField {
	private String labelCaption;

	private LookupFieldQueryBuilderThingy queryBuilderThingy;

	public CustomSearchField(String labelCaption, LookupFieldQueryBuilderThingy queryBuilderThingy) {
		super();
		this.labelCaption = labelCaption;
		this.queryBuilderThingy = queryBuilderThingy;
	}

	public String getLabelCaption() {
		return labelCaption;
	}

	public LookupFieldQueryBuilderThingy getQueryBuilderThingy() {
		return queryBuilderThingy;
	}
}
