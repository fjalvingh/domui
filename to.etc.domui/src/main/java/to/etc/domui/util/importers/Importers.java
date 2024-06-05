package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.exporters.ExcelFormat;
import to.etc.util.FileTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@NonNullByDefault
final public class Importers {
	@Nullable
	static public IRowReader createImporter(File file, @Nullable String originalName, boolean hasHeader) throws Exception {
		String name = originalName == null ? file.getName() : originalName;
		String ext = FileTool.getFileExtension(name).toLowerCase();
		switch(ext) {
			default:
				return null;

			case "csv":
				CsvRowReader rr = new CsvRowReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
				if(hasHeader)
					rr.hasHeader();
				return rr;

			case "xls":
				ExcelRowReader rr2 = new ExcelRowReader(new FileInputStream(file), ExcelFormat.XLS);
				if(hasHeader)
					rr2.setHeaderRowCount(1);
				return rr2;

			case "xlsx":
				rr2 = new ExcelRowReader(new FileInputStream(file), ExcelFormat.XLSX);
				if(hasHeader)
					rr2.setHeaderRowCount(1);
				return rr2;
		}
	}

}
