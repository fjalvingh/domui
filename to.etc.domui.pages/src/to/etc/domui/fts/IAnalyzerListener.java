package to.etc.domui.fts;

public interface IAnalyzerListener {
	public void		nextWord(String word, int sentence, int wordindex) throws Exception;
	public void		stopWord(String word, int sentence, int wordindex) throws Exception;
}
