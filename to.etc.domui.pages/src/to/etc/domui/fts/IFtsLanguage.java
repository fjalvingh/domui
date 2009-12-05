package to.etc.domui.fts;

public interface IFtsLanguage {
	public boolean	isStopWord(String word);
	public IStemmer	getStemmer();
	

}
