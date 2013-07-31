package to.etc.launcher.misc;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import to.etc.util.*;

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
			if(file.isDirectory()) {
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

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = null;
		Document doc = null;

		//code contains some new line formatting since it simply does not work proprly otherwise :(
		try {
			is = new StringInputStream("<testng-results>\n\t<suite name=\"" + namePart + "\">\n\t</suite>\n</testng-results>\n", "UTF-8");
			doc = builder.parse(is);
		} finally {
			is.close();
		}
		Node suite = XmlHelper.locateDirectChild(doc.getDocumentElement(), "suite");

		for (File partialReport : xmlFiles) {
			Document partialDoc = builder.parse(partialReport);
			Node partialSuite = XmlHelper.locateDirectChild(partialDoc.getDocumentElement(), "suite");
			Node partialTest = XmlHelper.locateDirectChild((Element) partialSuite, "test");
			Node movingNode = doc.importNode(partialTest, true);
			suite.appendChild(movingNode);
			suite.appendChild(doc.createTextNode("\n\t"));
		}

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		//transformer.setParameter(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		Result output = new StreamResult(singleFile);
		Source input = new DOMSource(doc);

		transformer.transform(input, output);

	}

}
