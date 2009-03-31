package to.etc.domui.util.images.converters;

import java.util.*;

public interface IImageConverter {
	public int			accepts(String inputmime, List<IImageConversionSpecifier> conversions) throws Exception;
	public void			convert(ImageConverterHelper helper, List<IImageConversionSpecifier> convs) throws Exception;
}
