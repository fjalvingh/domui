package to.etc.domui.themes.sass;

import to.etc.domui.state.IPageParameters;

import java.util.Map;

public interface IThemeVariablesCalculator {
	Map<String, String> calculate(IPageParameters parameters);

}
