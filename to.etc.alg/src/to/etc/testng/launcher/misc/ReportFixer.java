package to.etc.testng.launcher.misc;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

/**
 * Custom tool that collects separate reports into one big report.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class ReportFixer {

	public void assambleSingleReports(@Nonnull File reportsRoot) throws Exception {
		for(File file : reportsRoot.listFiles()) {
			if(file.isDirectory() && !(file.getName().startsWith("gen"))) {
				//root sub dirs are per browser
				assambleSingleReport(file);
			}
		}
	}

	private void assambleSingleReport(@Nonnull File reportRoot) throws Exception {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xml")) {
					return true;
				}
				return false;
			}
		};

		List<File> xmlFiles = new ArrayList<File>(Arrays.asList(reportRoot.listFiles(filter)));

		Collections.sort(xmlFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		String namePart = reportRoot.getParentFile().getName() + "_" + reportRoot.getName();
		File singleFile = new File(reportRoot, "report" + namePart + ".xml");

		//code contains some new line formatting since it simply does not work proprly otherwise :(
		Document doc = XmlHelper.getInstance().parseString("<testng-results>\n\t<suite name=\"" + namePart + "\">\n\t</suite>\n</testng-results>\n");
		Node suite = XmlHelper.getInstance().locateDirectChild(doc.getDocumentElement(), "suite");

		for (File partialReport : xmlFiles) {
			Document partialDoc = XmlHelper.getInstance().parseFile(partialReport);
			Node partialSuite = XmlHelper.getInstance().locateDirectChild(partialDoc.getDocumentElement(), "suite");
			Node partialTest = XmlHelper.getInstance().locateDirectChild((Element) partialSuite, "test");
			Node movingNode = doc.importNode(partialTest, true);
			suite.appendChild(movingNode);
			suite.appendChild(doc.createTextNode("\n\t"));
		}

		XmlHelper.getInstance().saveToFile(doc, singleFile);

		for(File partialReport : xmlFiles) {
			partialReport.delete();
		}

	}

}
